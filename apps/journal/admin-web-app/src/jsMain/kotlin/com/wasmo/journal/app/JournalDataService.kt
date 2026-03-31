package com.wasmo.journal.app

import com.wasmo.common.tokens.newToken
import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.EntrySummary
import com.wasmo.journal.api.ListEntriesRequest
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.Visibility
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
    val value = MutableStateFlow<List<EntrySummary>>(listOf())

    fun start() {
      scope.launch {
        val response = api.listEntries(ListEntriesRequest())
        value.value = response.entries
      }
    }
  }

  inner class EntryDataService internal constructor(
    val token: String,
    val initialValue: EntrySnapshot? = null,
  ) {
    private val mutableValue = MutableStateFlow(
      initialValue?.toViewModel(SaveState.Saved)
        ?: EntryViewModel(saveState = SaveState.Loading),
    )

    val value: StateFlow<EntryViewModel>
      get() = mutableValue

    fun setVisibility(visibility: Visibility) {
      mutableValue.update {
        it.copy(
          saveState = SaveState.Dirty,
          visibility = visibility,
        )
      }
    }

    fun setSlug(slug: String) {
      mutableValue.update {
        it.copy(
          saveState = SaveState.Dirty,
          slug = slug,
        )
      }
    }

    fun setTitle(title: String) {
      mutableValue.update {
        it.copy(
          saveState = SaveState.Dirty,
          title = title,
        )
      }
    }

    fun setBody(body: String) {
      mutableValue.update {
        it.copy(
          saveState = SaveState.Dirty,
          body = body,
        )
      }
    }

    fun start() {
      scope.launch {
        var latest = initialValue
          ?: run {
            val response = api.getEntry(token)
            mutableValue.value = response.toViewModel(SaveState.Saved)
            response
          }

        value
          .debounce(500.milliseconds)
          .filter { it.saveState == SaveState.Dirty }
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
              api.saveEntry(token, request)
              latest = request.entry
            }

            mutableValue.update {
              if (it == viewModel) it.copy(saveState = SaveState.Saved) else it
            }
          }
      }
    }
  }

  private fun EntrySnapshot.toViewModel(saveState: SaveState) = EntryViewModel(
    saveState = saveState,
    visibility = visibility,
    slug = slug,
    title = title,
    body = body,
  )
}
