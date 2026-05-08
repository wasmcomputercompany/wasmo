package com.wasmo.client.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.size
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.columnGap
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.gridColumn
import org.jetbrains.compose.web.css.gridRow
import org.jetbrains.compose.web.css.gridTemplateColumns
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLParagraphElement

enum class FormState {
  Ready,
  Busy,
}

val LocalFormState = compositionLocalOf { FormState.Ready }

@Composable
fun Form(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  content: @Composable () -> Unit,
) {
  val localFormState = LocalFormState.current
  Div(
    attrs = {
      classes(
        "Form",
        when (localFormState) {
          FormState.Ready -> "FormReady"
          FormState.Busy -> "FormBusy"
        },
      )
      style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Stretch)
      }
      attrs()
    },
  ) {
    content()
  }
}

@Composable
fun FormWasmoLogo(
  attrs: AttrsScope<HTMLImageElement>.() -> Unit = {},
) {
  Img(
    src = "/assets/wasmo1000x300.svg",
    alt = "Wasmo",
    attrs = {
      style {
        alignSelf(AlignSelf.Center)
        width(350.px)
        height(105.px)
      }
      attrs()
    },
  )
}

@Composable
fun PrimaryButton(
  attrs: AttrsScope<HTMLButtonElement>.() -> Unit,
  content: ContentBuilder<HTMLButtonElement>? = null,
) {
  val localFormState = LocalFormState.current
  Button(
    attrs = {
      classes("Primary")
      if (localFormState == FormState.Busy) {
        disabled()
      }
      attrs()
    },
    content = content,
  )
}

@Composable
fun TextField(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  label: String? = null,
  type: InputType<String> = InputType.Text,
  inputAttrs: InputAttrsScope<String>.() -> Unit,
) {
  val localFormState = LocalFormState.current
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
      if (localFormState == FormState.Busy) {
        disabled()
      }
      inputAttrs()
    }
  }
}

@Composable
fun Checkbox(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  label: String,
  type: InputType<Boolean> = InputType.Checkbox,
  inputAttrs: InputAttrsScope<Boolean>.() -> Unit,
  content: ContentBuilder<HTMLDivElement>,
) {
  val localFormState = LocalFormState.current
  Div(
    attrs = {
      style {
        display(DisplayStyle.Grid)
        gridTemplateColumns("auto 1fr")
        columnGap(8.px)
      }
      attrs()
    },
  ) {
    Input(
      type = type,
      attrs = {
        if (localFormState == FormState.Busy) {
          disabled()
        }
        style {
          gridColumn("1")
          gridRow("1")
          alignSelf("first baseline")
        }
        inputAttrs()
      },
    )
    H4(
      attrs = {
        classes("CheckboxLabel")
        style {
          gridColumn("2")
          gridRow("1")
          alignSelf("first baseline")
        }
      },
    ) {
      Text(label)
    }
    Div(
      attrs = {
        style {
          gridColumn("2")
          gridRow("2")
        }
        attrs()
      },
    ) {
      content()
    }
  }
}

@Composable
fun SmallText(
  attrs: AttrsScope<HTMLParagraphElement>.() -> Unit = {},
  content: ContentBuilder<HTMLParagraphElement>,
) {
  P(
    attrs = {
      classes("SmallText")
      attrs()
    },
  ) {
    content()
  }
}
