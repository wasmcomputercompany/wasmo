package com.wasmo.journal.app

import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.EntrySummary
import com.wasmo.journal.api.ListEntriesRequest
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.Visibility
import com.wasmo.support.tokens.newToken
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.files.FileList
import org.w3c.files.get

class JournalDataService(
  val clock: Clock,
  val scope: CoroutineScope,
  val api: JournalApi,
) {
  val summaries = EntrySummariesService()
    .also { it.start() }
  val publishService = PublishDataService()
    .also { it.start() }

  private val entries = mutableMapOf<String, EntryDataService>()

  fun publishSite() {
    publishService.requestPublish()
  }

  fun entry(token: String): EntryDataService {
    return entries.getOrPut(token) {
      EntryDataService(token)
        .also { it.start() }
    }
  }

  fun newEntry(): EntryDataService {
    val token = newToken()
    val entry = EntrySnapshot(
      token = token,
      visibility = Visibility.Private,
      slug = "",
      title = "",
      date = clock.now(),
      body = "",
    )
    return entries.getOrPut(token) {
      EntryDataService(token, entry)
        .also { it.start() }
    }
  }

  inner class PublishDataService {
    private val mutableValue = MutableStateFlow(
      PublishStateViewModel(
        publishNeeded = false,
        publishRequested = false,
      ),
    )

    val value: StateFlow<PublishStateViewModel>
      get() = mutableValue

    fun start() {
      scope.launch {
        val initialPublishState = api.requestPublish()
        mutableValue.update {
          it.copy(
            publishNeeded = it.publishNeeded || initialPublishState.publishNeededAt != null,
          )
        }

        value
          .filter { it.publishRequested }
          .collectLatest {
            api.requestPublish()
            // TODO: show a spinner until the publish completes.
            mutableValue.update {
              it.copy(
                publishNeeded = false,
                publishRequested = false,
              )
            }
          }
      }
    }

    fun requestPublish() {
      mutableValue.update {
        it.copy(publishRequested = true)
      }
    }

    fun onEntrySaved(previous: EntrySnapshot, latest: EntrySnapshot) {
      mutableValue.update {
        it.copy(
          publishNeeded = it.publishNeeded ||
            previous.visibility == Visibility.Published ||
            latest.visibility == Visibility.Published,
        )
      }
    }
  }

  inner class EntrySummariesService {
    private val mutableValue = MutableStateFlow(
      EntryListViewModel(
        syncState = SyncState.Loading,
        entries = listOf(),
      ),
    )

    val value: StateFlow<EntryListViewModel>
      get() = mutableValue

    fun onEntrySaved(entry: EntrySnapshot) {
      mutableValue.update { oldViewModel ->
        val index = oldViewModel.entries.indexOfFirst { it.token == entry.token }
        if (index == -1) {
          oldViewModel.copy(
            entries = listOf(entry.toSummary()) + oldViewModel.entries,
            syncState = SyncState.Loading,
          )
        } else {
          val newEntries = oldViewModel.entries.toMutableList()
          newEntries[index] = entry.toSummary()
          oldViewModel.copy(entries = newEntries)
        }
      }
    }

    fun start() {
      scope.launch {
        value
          .filter { it.syncState == SyncState.Loading }
          .collectLatest { viewModel ->
            val response = api.listEntries(ListEntriesRequest())
            mutableValue.value = EntryListViewModel(
              syncState = SyncState.Ready,
              entries = response.entries,
            )

            mutableValue.update {
              if (it == viewModel) it.copy(syncState = SyncState.Ready) else it
            }
          }
      }
    }
  }

  inner class EntryDataService internal constructor(
    val token: String,
    val initialValue: EntrySnapshot? = null,
  ) {
    private val mutableValue = MutableStateFlow(
      initialValue?.toViewModel(SyncState.Ready)
        ?: EntryViewModel(syncState = SyncState.Loading),
    )

    val value: StateFlow<EntryViewModel>
      get() = mutableValue

    private val mutableUploads = MutableStateFlow(mapOf<String, UploadViewModel>())

    val uploads: StateFlow<Map<String, UploadViewModel>>
      get() = mutableUploads

    fun setVisibility(visibility: Visibility) {
      mutableValue.update {
        it.copy(
          syncState = SyncState.Dirty,
          visibility = visibility,
        )
      }
    }

    fun setSlug(slug: String) {
      mutableValue.update {
        it.copy(
          syncState = SyncState.Dirty,
          slug = slug,
        )
      }
    }

    fun setTitle(title: String) {
      mutableValue.update {
        it.copy(
          syncState = SyncState.Dirty,
          title = title,
        )
      }
    }

    fun setBody(body: String) {
      mutableValue.update {
        it.copy(
          syncState = SyncState.Dirty,
          body = body,
        )
      }
    }

    fun start() {
      scope.launch {
        var latest = initialValue
          ?: run {
            val response = api.getEntry(token)
            mutableValue.value = response.toViewModel(SyncState.Ready)
            response
          }

        value
          .debounce(500.milliseconds)
          .filter { it.syncState == SyncState.Dirty }
          .collectLatest { viewModel ->
            val request = SaveEntryRequest(
              entry = latest.copy(
                title = viewModel.title,
                slug = viewModel.slug,
                body = viewModel.body,
                visibility = viewModel.visibility,
              ),
            )

            if (request.entry != latest) {
              val previous = latest
              try {
                api.saveEntry(token, request)
              } catch (e: Exception) {
                mutableValue.update {
                  it.copy(syncState = SyncState.Error(message = e.toString()))
                }
                return@collectLatest
              }
              latest = request.entry

              summaries.onEntrySaved(request.entry)
              publishService.onEntrySaved(previous, latest)
            }

            mutableValue.update {
              if (it == viewModel) it.copy(syncState = SyncState.Ready) else it
            }
          }
      }
    }

    fun addAttachments(bodyElementId: String, files: FileList) {
      for (i in 0 until files.length) {
        val file = files[i] ?: continue
        val attachmentToken = newToken()
        val url = "/api/entries/$token/attachments/$attachmentToken"

        // Add an <img> tag to the document body that links to the image being uploaded. If the
        // upload fails, this will be a broken link! Our Compose input elements use 'uncontrolled'
        // mode, so they won't show a new value if we edit it through a state variable.
        val bodyElement = document.getElementById(bodyElementId) as HTMLTextAreaElement?
        if (bodyElement != null) {
          bodyElement.value += """
            |
            |<img src="$url">
            |
            """.trimMargin()
        }

        scope.launch {
          supervisorScope {
            mutableUploads[attachmentToken] = UploadViewModel.Progress()
            val job = launch {
              api.addAttachment(
                entryToken = token,
                attachmentToken = attachmentToken,
                file = file,
                onProgress = { loaded, total ->
                  mutableUploads[attachmentToken] = UploadViewModel.Progress(
                    loaded = loaded.toDouble(),
                    total = total.toDouble(),
                  )
                },
              )
            }
            job.invokeOnCompletion { throwable ->
              mutableUploads[attachmentToken] = when {
                throwable != null -> UploadViewModel.Failed(throwable)
                else -> UploadViewModel.Success(url)
              }
            }
          }
        }
      }
    }
  }

  private fun EntrySnapshot.toViewModel(syncState: SyncState) = EntryViewModel(
    syncState = syncState,
    visibility = visibility,
    slug = slug,
    title = title,
    body = body,
  )

  private fun EntrySnapshot.toSummary() = EntrySummary(
    token = token,
    visibility = visibility,
    slug = slug,
    title = title,
    date = date,
  )

  operator fun <K, V> MutableStateFlow<Map<K, V>>.set(key: K, value: V) {
    update {
      it + mapOf(key to value)
    }
  }
}
