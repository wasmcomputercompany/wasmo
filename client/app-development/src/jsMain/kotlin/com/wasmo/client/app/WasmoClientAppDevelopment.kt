package com.wasmo.client.app

import kotlinx.browser.window

@JsExport
fun startOnLoad() {
  window.onload = {
    val app = WasmoClientApp(
      environment = DevelopmentEnvironment,
    )
    app.start()
  }
}

object DevelopmentEnvironment : Environment {
  override val warningLabel: String
    get() = "localhost"
}

