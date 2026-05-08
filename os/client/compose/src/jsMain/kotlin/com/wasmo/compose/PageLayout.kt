package com.wasmo.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.overflowY
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.DOMScope
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

/**
 * The content scrolls if necessary, and is vertically-centered otherwise.
 */
@Composable
fun PageLayout(
  attrs: AttrBuilderContext<HTMLDivElement> = {},
  header: (@Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit)? = null,
  content: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit,
) {
  Column(
    attrs = {
      classes("PageLayout")
      attrs()
    },
  ) {
    Column(
      attrs = {
        classes("PageLayoutScroller")
        style {
          overflowY("scroll")
        }
      },
    ) {
      if (header != null) {
        header {
          classes("PageLayoutHeader")
        }
      }
      content {
        classes("PageLayoutContent")
      }
    }
  }
}
