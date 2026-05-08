package com.wasmo.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.dom.Article
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Header
import org.w3c.dom.HTMLElement

@Composable
fun Dialog(
  attrs: AttrsScope<HTMLElement>.() -> Unit = {},
  visible: Boolean,
  content: @Composable DOMScope<*>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit,
) {
  Column(
    alignItems = AlignItems.Center,
    justifyContent = JustifyContent.Center,
    attrs = {
      style {
        property("pointer-events", "none")
      }
      attrs()
    },
  ) {
    Article(
      attrs = {
        if (visible) {
          classes("Dialog", "DialogVisible")
        } else {
          classes("Dialog", "DialogInvisible")
        }
      },
    ) {
      content {}
    }
  }
}

@Composable
fun DialogCloseHeader(
  onDismiss: () -> Unit,
) {
  Header {
    Toolbar(
      right = {
        ToolbarImageButton(
          image40x64Path = "/assets/close40x64.svg",
          altLabel = "Dismiss",
          onClick = {
            onDismiss()
          },
        )
      },
    )
  }
}
