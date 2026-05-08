package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.client.app.home.HomeEvent
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.smartphoneframe.SmartphoneFrame
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.dom.Iframe
import org.w3c.dom.HTMLDivElement

@Composable
fun ComputerList(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  items: List<Item>,
  eventListener: (HomeEvent) -> Unit,
) {
  for (item in items) {
    SmartphoneFrame(
      attrs = {
        style {
          alignSelf(AlignSelf.Center)
        }
        onClick {
          eventListener(HomeEvent.ClickComputer(item.slug))
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

class Item(
  val slug: ComputerSlug,
  val iframeSrc: String,
)
