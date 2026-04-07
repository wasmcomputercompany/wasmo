package com.wasmo.journal.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wasmo.journal.app.JournalDataService.EntryDataService
import com.wasmo.support.router.Router

class EditEntryScreen(
  private val router: Router<Route>,
  private val journalDataService: JournalDataService,
  private val entryDataService: EntryDataService,
) {
  @Composable
  fun Show() {
    val viewModel by entryDataService.value.collectAsState()
    val uploads by entryDataService.uploads.collectAsState()
    val publishState by journalDataService.publishService.value.collectAsState()
    EditEntry(
      syncState = viewModel.syncState,
      publishState = publishState,
      title = viewModel.title,
      slug = viewModel.slug,
      visibility = viewModel.visibility,
      body = viewModel.body,
      uploads = uploads,
      eventListener = { event ->
        when (event) {
          is EditEntryEvent.SetVisibility -> {
            entryDataService.setVisibility(event.value)
          }

          EditEntryEvent.PublishSite -> {
            journalDataService.publishSite()
          }

          is EditEntryEvent.EditTitle -> {
            entryDataService.setTitle(event.value)
          }

          is EditEntryEvent.EditSlug -> {
            entryDataService.setSlug(event.value)
          }

          is EditEntryEvent.EditBody -> {
            entryDataService.setBody(event.value)
          }

          EditEntryEvent.ClickBack -> {
            router.goTo(Route.Admin, Router.Direction.Pop)
          }

          is EditEntryEvent.AddAttachments -> {
            entryDataService.addAttachments(event.bodyElementId, event.files)
          }
        }
      },
    )
  }
}
