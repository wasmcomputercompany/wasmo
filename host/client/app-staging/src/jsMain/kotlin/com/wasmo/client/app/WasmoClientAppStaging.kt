package com.wasmo.client.app

import kotlinx.browser.window

@JsExport
fun startOnLoad() {
  window.onload = {
    val app = createWasmoClientApp(
      environment = StagingEnvironment,
    )
    app.start()
  }
}

object StagingEnvironment : Environment {
  override val passkeyUser: String
    get() = "passkeys@wasmo.dev"

  override val warningLabel: String
    get() = "wasmo.dev"

  override val showSignUp: Boolean
    get() = false
}

