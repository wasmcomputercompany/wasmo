package com.wasmo.client.app

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.jetbrains.compose.web.attributes.size
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.background
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.overflowY
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun FormScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  content: @Composable () -> Unit,
) {
  Div(
    attrs = {
      classes("FormScreen")
      style {
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
      attrs()
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
          property("width", "min(100%, 420px)")
        }
      },
    ) {
      content()
    }
  }
}

@Composable
fun PrimaryButton(
  attrs: InputAttrsScope<Unit>.() -> Unit,
) {
  Input(
    type = InputType.Button,
  ) {
    classes("Primary")
    attrs()
  }
}

@Composable
fun SecondaryButton(
  attrs: InputAttrsScope<Unit>.() -> Unit,
) {
  Input(
    type = InputType.Button,
  ) {
    classes("Secondary")
    attrs()
  }
}

@Composable
fun TextField(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  label: String? = null,
  type: InputType<String> = InputType.Text,
  inputAttrs: InputAttrsScope<String>.() -> Unit,
) {
  Div(
    attrs = {
      style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Stretch)
      }
      attrs()
    },
  ) {
    if (label != null) {
      P(
        attrs = {
          style {
            margin(0.px, 16.px)
            property("text-transform", "uppercase")
            property("color", "rgb(255 255 255 / 0.8)")
          }
        },
      ) {
        Text(label)
      }
    }
    Input(
      type = type,
    ) {
      size(6)
      inputAttrs()
    }
  }
}
