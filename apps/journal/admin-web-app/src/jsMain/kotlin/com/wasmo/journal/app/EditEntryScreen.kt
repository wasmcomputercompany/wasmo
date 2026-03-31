package com.wasmo.journal.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wasmo.journal.api.Visibility
import com.wasmo.journal.app.JournalDataService.EntryDataService
import com.wasmo.journal.app.util.Router

class EditEntryScreen(
  private val router: Router<JournalRoute>,
  private val entryDataService: EntryDataService,
) {
  @Composable
  fun Show() {
    val viewModel by entryDataService.value.collectAsState()
    EditEntry(
      saveState = viewModel.saveState,
      title = viewModel.title,
      slug = viewModel.slug,
      visibility = viewModel.visibility,
      body = viewModel.body,
      eventListener = { event ->
        when (event) {
          EditEntryEvent.ClickPublish -> {
            entryDataService.setVisibility(Visibility.Published)
          }

          EditEntryEvent.ClickUnpublish -> {
            entryDataService.setVisibility(Visibility.Private)
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
            router.goTo(JournalRoute.AdminRoute, Router.Direction.Pop)
          }
        }
      },
    )
  }
}
