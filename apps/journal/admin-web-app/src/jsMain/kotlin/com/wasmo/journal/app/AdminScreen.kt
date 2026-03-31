package com.wasmo.journal.app

import androidx.compose.runtime.Composable
import com.wasmo.journal.api.EntrySummary
import com.wasmo.journal.api.Visibility
import com.wasmo.journal.app.util.Router
import kotlin.time.Instant

class AdminScreen(
  private val journalDataService: JournalDataService,
  private val router: Router<JournalRoute>,
) {
  @Composable
  fun Show() {
    val entries = listOf(
      EntrySummary(
        token = "aaaaabbbbbcccccdddddeeeee",
        visibility = Visibility.Private,
        slug = "wasm",
        title = "WebAssembly is like JSON for behaviour",
        date = Instant.fromEpochSeconds(0L),
      ),
    )

    EntryList(
      entries = entries,
      eventListener = { event ->
        when (event) {
          is EntryListEvent.ClickEntry -> {
            router.goTo(
              route = JournalRoute.EditEntryRoute(event.token),
              direction = Router.Direction.Push,
            )
          }

          EntryListEvent.NewEntry -> {
            val newEntry = journalDataService.newEntry()
            router.goTo(
              route = JournalRoute.EditEntryRoute(newEntry.token),
              direction = Router.Direction.Push,
            )
          }
        }
      },
    )
  }
}
