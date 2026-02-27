package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.api.routes.toURL
import com.wasmo.client.app.FormScreen
import com.wasmo.smartphoneframe.SmartphoneFrame
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.dom.Iframe
import org.w3c.dom.HTMLElement

@Composable
fun ComputerListScreen(
  attrs: AttrsScope<HTMLElement>.() -> Unit = {},
  items: List<ComputerListItem>,
  eventListener: (ComputerListEvent) -> Unit,
) {
  FormScreen(
    attrs = {
      classes("ComputerListScreen")
      attrs()
    },
  ) {
    for (item in items) {
      SmartphoneFrame(
        attrs = {
          onClick {
            eventListener(ComputerListEvent.ClickComputer(item.slug))
          }
          attrs()
        },
      ) { frameAttrs ->
        NameOverlay(
          name = item.slug,
          attrs = frameAttrs,
        ) { nameOverlayAttrs ->
          Iframe(
            attrs = {
              attr("src", item.url.toURL().href)
              style {
                border(0.px)
                backgroundColor(rgb(51, 51, 51))
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
