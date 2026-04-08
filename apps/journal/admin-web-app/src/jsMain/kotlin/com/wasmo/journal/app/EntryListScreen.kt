package com.wasmo.journal.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wasmo.support.router.Router
import kotlinx.browser.window

class EntryListScreen(
  private val journalDataService: JournalDataService,
  private val router: Router<Route>,
) {
  @Composable
  fun Show() {
    val publishState by journalDataService.publishService.value.collectAsState()
    val listViewModel by journalDataService.summaries.value.collectAsState()

    EntryList(
      entries = listViewModel.entries,
      publishState = publishState,
      eventListener = { event ->
        when (event) {
          EntryListEvent.ViewSite -> {
            window.open("/", "_self")
          }

          is EntryListEvent.ClickEntry -> {
            router.goTo(
              route = Route.EditEntry(event.token),
              direction = Router.Direction.Push,
            )
          }

          EntryListEvent.NewEntry -> {
            val newEntry = journalDataService.newEntry()
            router.goTo(
              route = Route.EditEntry(newEntry.token),
              direction = Router.Direction.Push,
            )
          }

          EntryListEvent.PublishSite -> {
            journalDataService.publishSite()
          }
        }
      },
    )
  }
}
