package com.wasmo.launcher

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
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun LauncherScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  content: @Composable () -> Unit,
) {
  Div(
    attrs = {
      classes("LauncherScreen")
      style {
        width(100.percent)
        height(100.percent)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Start)
      }
      attrs()
    },
  ) {
    content()
  }
}

@Composable
fun LauncherIconList(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  icons: @Composable () -> Unit,
) {
  Div(
    attrs = {
      classes("LauncherIconList")
      attrs()
    },
  ) {
    icons()
  }
}

/**
 * @param maskableIconUrl a square-aspect ratio image, at least 512x512 with a safe zone in the
 *   middle 80% of the icon.
 */
@Composable
fun Icon(
  label: String,
  maskableIconUrl: String,
  onClick: ((SyntheticMouseEvent) -> Unit)? = null,
) {
  Div(
    attrs = {
      classes("LauncherIcon")
      if (onClick != null) {
        onClick { event ->
          onClick(event)
        }
      }
    },
  ) {
    Img(
      src = maskableIconUrl,
      attrs = {
        classes("LauncherIconImage")
      },
    )
    Div(
      attrs = {
        classes("LauncherIconLabel")
      },
    ) {
      Text(label)
    }
  }
}
