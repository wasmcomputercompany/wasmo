package com.wasmo.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Fieldset
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Small
import org.jetbrains.compose.web.dom.Text

@Composable
fun TextField(
  label: String? = null,
  type: InputType<String> = InputType.Text,
  ariaLabel: String? = null,
  disabled: Boolean = false,
  caption: String? = null,
  inputAttrs: InputAttrsScope<String>.() -> Unit,
) {
  val inputId = rememberNextId()
  Label {
    if (label != null) {
      Text(label)
    }
    Input(
      type = type,
      attrs = {
        attr("aria-describedby", inputId)
        if (ariaLabel != null) {
          attr("aria-label", ariaLabel)
        }
        if (disabled) {
          disabled()
        }
        inputAttrs()
      },
    )
    if (caption != null) {
      Small(
        attrs = {
          id(inputId)
        },
      ) {
        Text(caption)
      }
    }
  }
}

@Composable
fun WasmoNameTextField(
  label: String? = null,
  ariaLabel: String? = null,
  disabled: Boolean = false,
  caption: String? = null,
  inputAttrs: InputAttrsScope<String>.() -> Unit,
) {
  val inputId = rememberNextId()
  Label {
    if (label != null) {
      Text(label)
    }
    Fieldset(
      attrs = {
        attr("role", "group")
      },
    ) {
      Input(
        type = InputType.Text,
        attrs = {
          attr("aria-describedby", inputId)
          if (ariaLabel != null) {
            attr("aria-label", ariaLabel)
          }
          if (disabled) {
            disabled()
          }
          inputAttrs()
        },
      )
      Input(
        type = InputType.Text,
        attrs = {
          attr("aria-describedby", inputId)
          if (ariaLabel != null) {
            attr("aria-label", ariaLabel)
          }
          disabled()
          defaultValue(".wasmo.com")
        },
      )
    }
    if (caption != null) {
      Small(
        attrs = {
          id(inputId)
        },
      ) {
        Text(caption)
      }
    }
  }
}
