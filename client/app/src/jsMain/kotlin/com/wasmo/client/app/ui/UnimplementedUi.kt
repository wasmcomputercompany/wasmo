package com.wasmo.client.app.ui

import androidx.compose.runtime.Composable
import com.wasmo.client.framework.Ui
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

class UnimplementedUi(
  val name: String,
) : Ui {
  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    H1 {
      Text(name)
    }
  }
}
