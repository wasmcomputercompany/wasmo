package com.wasmo.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

typealias ComposableElement = @Composable (
  attrs: AttrsScope<HTMLElement>.() -> Unit,
) -> Unit
