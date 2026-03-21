package com.wasmo.journal.app

import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposableInBody

fun start() {
  renderComposableInBody {
    H1 {
      Text("Journal App")
    }
  }
}
