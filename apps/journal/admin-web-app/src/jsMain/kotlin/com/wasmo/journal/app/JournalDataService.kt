package com.wasmo.journal.app

import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.EntrySummary
import com.wasmo.journal.api.ListEntriesRequest
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.Visibility
import com.wasmo.support.tokens.newToken
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JournalDataService(
  val clock: Clock,
  val scope: CoroutineScope,
  val api: JournalApi,
) {
  val summaries = EntrySummariesService()
    .also { it.start() }

  private val entries = mutableMapOf<String, EntryDataService>()

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
      version = 0L,
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
              expectedVersion = latest.version,
              entry = latest.copy(
                title = viewModel.title,
                slug = viewModel.slug,
                body = viewModel.body,
                visibility = viewModel.visibility,
                version = latest.version + 1L,
              ),
            )

            if (request.entry != latest) {
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
            }

            mutableValue.update {
              if (it == viewModel) it.copy(syncState = SyncState.Ready) else it
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
}
