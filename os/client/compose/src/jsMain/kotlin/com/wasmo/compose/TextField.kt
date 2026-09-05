package com.wasmo.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Small
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLFieldSetElement

@Composable
fun TextField(
  label: String? = null,
  ariaLabel: String? = null,
  disabled: Boolean = false,
  helperText: String? = null,
  groupedInputs: (@Composable DOMScope<HTMLFieldSetElement>.() -> Unit)? = null,
  type: InputType<String> = InputType.Text,
  inputAttrs: InputAttrsScope<String>.() -> Unit,
) {
  val inputId = rememberNextId()
  Label {
    if (label != null) {
      Text(label)
    }
    if (groupedInputs != null) {
      FieldsetGroup {
        Input(
          type = type,
          attrs = {
            describedBy(inputId)
            ariaLabel(ariaLabel)
            disabled(disabled)
            inputAttrs()
          },
        )
        groupedInputs()
      }
    } else {
      Input(
        type = type,
        attrs = {
          describedBy(inputId)
          ariaLabel(ariaLabel)
          disabled(disabled)
          inputAttrs()
        },
      )
    }

    if (helperText != null) {
      Small(
        attrs = {
          id(inputId)
        },
      ) {
        Text(helperText)
      }
    }
  }
}
