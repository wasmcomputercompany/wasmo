package com.wasmo.client.app

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.background
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.overflowY
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div

@Composable
fun FormScreen(
  childStyle: StyleScope.() -> Unit = {},
  content: @Composable () -> Unit,
) {
  Div(
    attrs = {
      classes("FormScreen")
      style {
        childStyle()
        background("#A100F1")
        background("linear-gradient(177deg, rgba(161, 0, 241, 1) 0%, rgba(20, 0, 105, 1) 100%)")
        width(100.percent)
        height(100.percent)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Start)
        overflowY("scroll")
      }
    },
  ) {
    Div(
      attrs = {
        style {
          display(DisplayStyle.Flex)
          flexDirection(FlexDirection.Column)
          alignItems(AlignItems.Stretch)
          justifyContent(JustifyContent.Start)
          boxSizing("border-box")
          padding(12.px)
          property("width", "min(100%, 420px)")
        }
      }
    ) {
      content()
    }
  }
}
