package com.wasmo.journal.app

import androidx.compose.runtime.Composable
import com.wasmo.support.router.Router
import kotlin.time.Clock
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposableInBody

@JsExport
fun startOnLoad() {
  window.onload = {
    start()
  }
}

fun start() {
  val clock = Clock.System
  val mainScope = MainScope()
  val journalApi = RealJournalApi()
  val journalDataService = JournalDataService(
    clock = clock,
    scope = mainScope,
    api = journalApi,
  )

  val router = Router(Route)
  router.start()

  renderComposableInBody {
    when (val route = router.current.value) {
      is Route.Admin -> {
        EntryListScreen(
          journalDataService = journalDataService,
          router = router,
        ).Show()
      }

      is Route.EditEntry -> {
        EditEntryScreen(
          router = router,
          journalDataService = journalDataService,
          entryDataService = journalDataService.entry(route.token),
        ).Show()
      }

      else -> {
        NotFound()
      }
    }
  }
}

@Composable
fun NotFound() {
  H1 {
    Text("Not Found!")
  }
}
