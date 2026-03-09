package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.client.app.FormScreen
import com.wasmo.identifiers.ComputerSlug
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
  items: List<Item>,
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
          name = item.slug.value,
          attrs = frameAttrs,
        ) { nameOverlayAttrs ->
          Iframe(
            attrs = {
              attr("src", item.iframeSrc)
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

class Item(
  val slug: ComputerSlug,
  val iframeSrc: String,
)

sealed interface ComputerListEvent {
  data class ClickComputer(val slug: ComputerSlug) : ComputerListEvent
}
