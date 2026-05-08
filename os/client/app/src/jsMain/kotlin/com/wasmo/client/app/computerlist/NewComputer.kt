package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.client.app.home.HomeEvent
import com.wasmo.compose.PicoButton
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Text

@Composable
fun NewComputer(
  attrs: AttrsScope<*>.() -> Unit = {},
  eventListener: (HomeEvent) -> Unit,
) {
  PicoButton(
    outline = true,
    contrast = true,
    attrs = {
      onClick {
        eventListener(HomeEvent.ClickNewComputer)
      }
      attrs()
    },
  ) {
    Text("New Computer")
  }
}
