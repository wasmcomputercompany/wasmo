package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.client.app.home.HomeEvent
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.smartphoneframe.SmartphoneFrame
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.overflowY
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Iframe
import org.w3c.dom.HTMLDivElement

@Composable
fun ComputerList(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  items: List<Item>,
  eventListener: (HomeEvent) -> Unit,
) {
  Div(
    attrs = {
      style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Start)
        overflowY("scroll")
        paddingBottom(48.px)
      }
      attrs()
    },
  ) {
    for (item in items) {
      SmartphoneFrame(
        attrs = {
          onClick {
            eventListener(HomeEvent.ClickComputer(item.slug))
          }
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
