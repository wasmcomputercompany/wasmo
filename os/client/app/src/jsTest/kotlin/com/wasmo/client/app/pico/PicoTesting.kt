package com.wasmo.client.app.pico

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.background
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

/**
 * Wrap [content] in a background-colored frame, because otherwise there isn't one.
 */
@Composable
fun ScreenFrame(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  content: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit,
) {
  Div(
    attrs = {
      style {
        background("var(--pico-background-color)")
        property("padding", "var(--pico-spacing)")
      }
      attrs()
    },
  ) {
    content {
    }
  }
}
