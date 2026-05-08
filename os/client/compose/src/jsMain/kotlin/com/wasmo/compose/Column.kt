package com.wasmo.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

@Composable
fun Column(
  attrs: AttrBuilderContext<HTMLDivElement> = {},
  alignItems: AlignItems? = null,
  justifyContent: JustifyContent? = null,
  content: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit,
) {
  Div(
    attrs = {
      classes("Column")
      style {
        if (alignItems != null) {
          alignItems(alignItems)
        }
        if (justifyContent != null) {
          justifyContent(justifyContent)
        }
      }
      attrs()
    },
  ) {
    content {
    }
  }
}
