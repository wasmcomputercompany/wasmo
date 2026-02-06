package app.rounds.client.app

import kotlinx.browser.window

@JsExport
fun startOnLoad() {
  window.onload = {
    val app = WasmComputerApp()
    app.start()
  }
}
