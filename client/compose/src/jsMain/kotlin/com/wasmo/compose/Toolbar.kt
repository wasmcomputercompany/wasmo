package com.wasmo.compose

import androidx.compose.runtime.Composable
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
import org.jetbrains.compose.web.dom.Div

@Composable
fun Toolbar(
  childStyle: ChildStyle,
  left: ComposableElement? = null,
  title: ComposableElement = {},
  right: ComposableElement? = null,
) {
  Div(
    attrs = {
      classes("Toolbar")
      style {
        childStyle()
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Start)
      }
    },
  ) {
    left?.invoke {
    }
    title {
      flex(100, 100, 0.px)
      marginLeft(
        when {
          left != null -> 8.px
          else -> 0.px
        },
      )
      marginRight(
        when {
          right != null -> 8.px
          else -> 0.px
        },
      )
    }
    right?.invoke {
    }
  }
}

@Composable
fun ToolbarTitle(
  childStyle: ChildStyle,
  content: @Composable () -> Unit,
) {
  Div(
    attrs = {
      classes("ToolbarTitle")
      style {
        childStyle()
      }
    },
  ) {
    content()
  }
}
