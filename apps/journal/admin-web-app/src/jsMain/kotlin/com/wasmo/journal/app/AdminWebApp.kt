package com.wasmo.journal.app

import androidx.compose.runtime.Composable
import com.wasmo.journal.app.util.Router
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposableInBody

@JsExport
fun startOnLoad() {
  window.onload = {
    start()
  }
}

fun start() {
  val router = Router(JournalRoute)
  router.start()

  renderComposableInBody {
    when (val route = router.current.value) {
      is JournalRoute.AdminRoute -> {
        Admin { token ->
          router.goTo(JournalRoute.EditEntryRoute(token), Router.Direction.Push)
        }
      }

      is JournalRoute.EditEntryRoute -> {
        EditEntry(route.token) {
          router.goTo(JournalRoute.AdminRoute, Router.Direction.Pop)
        }
      }

      else -> {
        NotFound()
      }
    }
  }
}

@Composable
fun Admin(
  onClickEntry: (String) -> Unit,
) {
  H1 {
    Text("Admin")
  }
  for (i in 0 until 3) {
    P {
      A(
        attrs = {
          onClick {
            onClickEntry("$i")
          }
        },
      ) {
        Text("ENTRY $i")
      }
    }
  }
}

@Composable
fun EditEntry(
  token: String,
  onClickBack: () -> Unit,
) {
  P {
    A(
      attrs = {
        onClick {
          onClickBack()
        }
      },
    ) {
      Text("< BACK")
    }
  }
  H1 {
    Text("Edit Entry $token")
  }
}

@Composable
fun NotFound() {
  H1 {
    Text("Not Found!")
  }
}
