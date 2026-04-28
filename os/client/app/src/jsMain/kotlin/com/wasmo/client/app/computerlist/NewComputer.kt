package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.client.app.Form
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.home.HomeEvent
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun NewComputer(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (HomeEvent) -> Unit,
) {
  Form(
    attrs = {
      style {
        justifyContent(JustifyContent.Center)
      }
      attrs()
    },
  ) {
    PrimaryButton(
      attrs = {
        onClick {
          eventListener(HomeEvent.ClickNewComputer)
        }
      },
    ) {
      Text("New Computer")
    }
  }
}
