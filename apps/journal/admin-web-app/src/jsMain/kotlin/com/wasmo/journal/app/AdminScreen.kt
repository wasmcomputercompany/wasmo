package com.wasmo.journal.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wasmo.journal.app.util.Router

class AdminScreen(
  private val journalDataService: JournalDataService,
  private val router: Router<JournalRoute>,
) {
  @Composable
  fun Show() {
    val listViewModel by journalDataService.summaries.value.collectAsState()

    EntryList(
      entries = listViewModel.entries,
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
