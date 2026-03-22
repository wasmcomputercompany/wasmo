package com.wasmo.journal.app

import kotlinx.browser.window
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposableInBody

@JsExport
fun startOnLoad() {
  window.onload = {
    start()
  }
}

fun start() {
  renderComposableInBody {
    H1 {
      Text("Journal App")
    }
  }
}
