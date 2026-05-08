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
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

@Composable
fun Toolbar(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  left: (@Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit)? = null,
  title: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit = { attrs -> ToolbarTitle(attrs) {} },
  right: (@Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit)? = null,
) {
  Div(
    attrs = {
      classes("Toolbar")
      style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Start)
      }
      attrs()
    },
  ) {
    left?.invoke(this) {
    }
    title {
      style {
        flex(100, 100, 0.px)
      }
    }
    right?.invoke(this) {
    }
  }
}

@Composable
fun ToolbarTitle(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  content: @Composable () -> Unit,
) {
  Div(
    attrs = {
      classes("ToolbarTitle")
      attrs()
    },
  ) {
    content()
  }
}

@Composable
fun ToolbarImageButton(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  image40x64Path: String,
  altLabel: String,
  onClick: ((SyntheticMouseEvent) -> Unit)? = null,
) {
  Div(
    attrs = {
      classes("ToolbarImageButton")
      style {
        property("mask", "url('$image40x64Path')")
      }
      if (onClick != null) {
        onClick { event ->
          event.stopPropagation()
          onClick(event)
        }
      }
      attrs()
    },
  )
}
