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
  override val passkeyUser: String
    get() = "wasmo-development-passkeys"

  override val warningLabel: String?
    get() = null

  override val showSignUp: Boolean
    get() = true
}
