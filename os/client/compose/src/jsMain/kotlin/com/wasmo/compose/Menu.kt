package com.wasmo.compose

import androidx.compose.runtime.Composable
import androidx.compose.web.events.SyntheticMouseEvent
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

@Composable
fun Menu(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  visible: Boolean,
  onDismiss: () -> Unit,
  content: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit,
) {
  Div(
    attrs = {
      style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Stretch)
        justifyContent(JustifyContent.End)
        property("pointer-events", "none")
      }
      attrs()
    },
  ) {
    Div(
      attrs = {
        classes("Menu")
        classes(
          when {
            visible -> "MenuVisible"
            else -> "MenuInvisible"
          },
        )

        style {
          display(DisplayStyle.Flex)
          flexDirection(FlexDirection.Column)
          alignItems(AlignItems.Stretch)
          justifyContent(JustifyContent.Start)
        }
      },
    ) {
      Toolbar(
        attrs = {},
        title = {},
        left = { toolbarChildAttrs ->
          ToolbarImageButton(
            attrs = toolbarChildAttrs,
            image40x64Path = "/assets/close40x64.svg",
            altLabel = "Dismiss",
            onClick = {
              onDismiss()
            },
          )
        },
      )

      content {
      }
    }
  }
}

@Composable
fun MenuItem(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  label: String,
  onClick: (SyntheticMouseEvent) -> Unit,
) {
  Div(
    attrs = {
      classes("MenuItem")
      onClick(onClick)
      attrs()
    },
  ) {
    Text(label)
  }
}
