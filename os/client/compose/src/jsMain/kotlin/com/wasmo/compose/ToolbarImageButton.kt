package com.wasmo.compose

import androidx.compose.runtime.Composable
import androidx.compose.web.events.SyntheticMouseEvent
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Img
import org.w3c.dom.HTMLImageElement

@Composable
fun ToolbarImageButton(
  attrs: AttrsScope<HTMLImageElement>.() -> Unit = {},
  image40x64Path: String,
  altLabel: String,
  onClick: ((SyntheticMouseEvent) -> Unit)? = null,
) {
  Img(
    src = image40x64Path,
    alt = altLabel,
    attrs = {
      classes("ToolbarImageButton")
      if (onClick != null) {
        onClick { event ->
          onClick(event)
        }
      }
      attrs()
    },
  )
}
