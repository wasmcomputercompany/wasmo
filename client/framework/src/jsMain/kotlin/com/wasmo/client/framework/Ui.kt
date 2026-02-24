package com.wasmo.client.framework

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

interface Ui {
  @Composable
  fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  )
}
