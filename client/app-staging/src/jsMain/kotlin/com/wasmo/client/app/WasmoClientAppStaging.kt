package com.wasmo.client.app

import kotlinx.browser.window

@JsExport
fun startOnLoad() {
  window.onload = {
    val app = WasmoClientApp(
      environment = StagingEnvironment,
    )
    app.start()
  }
}

object StagingEnvironment : Environment {
  override val warningLabel: String
    get() = "wasmo.dev"
}

