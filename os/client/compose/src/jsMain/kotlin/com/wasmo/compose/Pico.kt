package com.wasmo.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Small
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLButtonElement

@Composable
fun PicoTextField(
  label: String,
  type: InputType<String> = InputType.Text,
  ariaLabel: String? = null,
  disabled: Boolean = false,
  caption: String? = null,
  inputAttrs: InputAttrsScope<String>.() -> Unit,
) {
  val inputId = rememberNextId()
  Label {
    Text(label)
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
fun PicoButton(
  attrs: AttrsScope<HTMLButtonElement>.() -> Unit,
  busy: Boolean = false,
  disabled: Boolean = false,
  content: ContentBuilder<HTMLButtonElement> = {},
) {
  Button(
    attrs = {
      if (busy) {
        attr("aria-busy", "true")
      }
      if (disabled) {
        disabled()
      }
      attrs()
    },
  ) {
    content()
  }
}

internal val LocalIdGenerator = compositionLocalOf { IdGenerator() }

/**
 * Generate a unique HTML element ID like `id100`.
 */
@Composable
fun rememberNextId(): String {
  val idGenerator = LocalIdGenerator.current
  return remember {
    idGenerator.nextId()
  }
}

internal class IdGenerator {
  private var nextId: Int = 1000

  fun nextId() = "id${nextId++}"
}
