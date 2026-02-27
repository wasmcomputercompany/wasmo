package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.client.app.FormScreen
import com.wasmo.smartphoneframe.SmartphoneFrame
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Iframe
import org.w3c.dom.HTMLElement

@Composable
fun ComputerListScreen(
  attrs: AttrsScope<HTMLElement>.() -> Unit,
  eventListener: (ComputerListEvent) -> Unit,
) {
  FormScreen(
    attrs = {
      classes("ComputerListScreen")
      attrs()
    },
  ) {
    for (i in 96..99) {
      val slug = "jesse$i"
      val url = "http://$slug.localhost:8080/"
      SmartphoneFrame(
        attrs = {
          onClick {
            eventListener(ComputerListEvent.ClickComputer(slug))
          }
          attrs()
        },
      ) { frameAttrs ->
        NameOverlay(
          name = slug,
          attrs = frameAttrs,
        ) { nameOverlayAttrs ->
          Iframe(
            attrs = {
              attr("src", url)
              style {
                border(0.px)
              }
              nameOverlayAttrs()
            },
          )
        }
      }
    }
  }
}

sealed interface ComputerListEvent {
  data class ClickComputer(val slug: String) : ComputerListEvent
}
