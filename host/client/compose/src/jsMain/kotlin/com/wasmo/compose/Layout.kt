package com.wasmo.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

@Composable
fun Zstack(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit,
  content: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit,
) {
  Div(
    attrs = {
      style {
        position(Position.Relative)
        property("z-index", "0")
      }
      attrs()
    },
  ) {
    content {
      style {
        position(Position.Absolute)
        width(100.percent)
        height(100.percent)
        boxSizing("border-box")
        overflow("clip")
      }
    }
  }
}
