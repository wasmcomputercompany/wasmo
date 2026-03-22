package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.compose.Zstack
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

/**
 * Puts a dark scrim over [content], and puts [name] in front.
 */
@Composable
fun NameOverlay(
  name: String,
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  content: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit,
) {
  Zstack(
    attrs = {
      style {
        width(100.percent)
        height(100.percent)
        boxSizing("border-box")
      }
      attrs()
    },
  ) { zstackChildAttrs ->
    content(zstackChildAttrs)

    Div(
      attrs = {
        classes("NameOverlay")
        style {
          display(DisplayStyle.Flex)
          flexDirection(FlexDirection.Column)
          justifyContent(JustifyContent.Center)
          alignItems(AlignItems.Stretch)
          textAlign("center")
        }
        zstackChildAttrs()
      },
    ) {
      Text(name)
    }
  }
}
