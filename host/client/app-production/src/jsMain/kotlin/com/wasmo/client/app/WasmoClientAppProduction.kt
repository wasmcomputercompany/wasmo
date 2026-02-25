package com.wasmo.client.app

import kotlinx.browser.window

@JsExport
fun startOnLoad() {
  window.onload = {
    val app = newWasmoClientApp(
      environment = ProductionEnvironment,
    )
    app.start()
  }
}

object ProductionEnvironment : Environment {
  override val passkeyUser: String
    get() = "passkeys@wasmo.com"

  override val warningLabel: String?
    get() = null

  override val showSignUp: Boolean
    get() = false
}
