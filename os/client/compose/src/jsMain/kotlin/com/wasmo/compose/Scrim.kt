package com.wasmo.compose

import androidx.compose.runtime.Composable
import androidx.compose.web.events.SyntheticMouseEvent
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement

@Composable
fun Scrim(
  visible: Boolean,
  onClick: ((SyntheticMouseEvent) -> Unit)? = null,
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
) {
  Div(
    attrs = {
      classes("Scrim")
      classes(
        when {
          visible -> "ScrimVisible"
          else -> "ScrimInvisible"
        },
      )

      if (onClick != null) {
        onClick { event ->
          onClick(event)
        }
      }

      attrs()
    },
  )
}
