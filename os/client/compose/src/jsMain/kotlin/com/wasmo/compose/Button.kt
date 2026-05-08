package com.wasmo.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.ContentBuilder
import org.w3c.dom.HTMLButtonElement

@Composable
fun Button(
  attrs: AttrsScope<HTMLButtonElement>.() -> Unit = {},
  outline: Boolean = false,
  contrast: Boolean = false,
  busy: Boolean = false,
  disabled: Boolean = false,
  content: ContentBuilder<HTMLButtonElement> = {},
) {
  Button(
    attrs = {
      if (outline) {
        classes("outline")
      }
      if (contrast) {
        classes("contrast")
      }
      if (busy) {
        attr("aria-busy", "true")
      }
      if (disabled) {
        disabled()
      }
      attrs()
    },
  ) {
    content()
  }
}
