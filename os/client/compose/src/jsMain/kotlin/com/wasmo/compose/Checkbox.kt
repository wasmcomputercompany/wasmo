package com.wasmo.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.columnGap
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.gridColumn
import org.jetbrains.compose.web.css.gridRow
import org.jetbrains.compose.web.css.gridTemplateColumns
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

/**
 * A checkbox with multiple lines of explanation on the entry.
 */
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
        display(DisplayStyle.Companion.Grid)
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
