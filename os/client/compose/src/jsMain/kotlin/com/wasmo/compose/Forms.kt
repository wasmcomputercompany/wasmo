package com.wasmo.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Fieldset
import org.w3c.dom.HTMLFieldSetElement

/**
 * https://picocss.com/docs/group
 */
@Composable
fun FieldsetGroup(
  attrs: AttrsScope<HTMLFieldSetElement>.() -> Unit = {},
  content: @Composable DOMScope<HTMLFieldSetElement>.() -> Unit,
) {
  Fieldset(
    attrs = {
      attr("role", "group")
      attrs()
    },
  ) {
    content()
  }
}

fun InputAttrsScope<String>.disabled(disabled: Boolean) {
  if (disabled) {
    disabled()
  }
}

fun InputAttrsScope<String>.ariaLabel(ariaLabel: String?) {
  if (ariaLabel != null) {
    attr("aria-label", ariaLabel)
  }
}

fun InputAttrsScope<String>.describedBy(inputId: String) {
  attr("aria-describedby", inputId)
}
