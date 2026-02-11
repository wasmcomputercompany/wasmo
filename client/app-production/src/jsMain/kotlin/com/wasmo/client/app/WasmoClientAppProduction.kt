package com.wasmo.client.app

import kotlinx.browser.window

@JsExport
fun startOnLoad() {
  window.onload = {
    val app = WasmoClientApp(
      environment = ProductionEnvironment,
    )
    app.start()
  }
}

object ProductionEnvironment : Environment {
  override val warningLabel: String?
    get() = null
}
