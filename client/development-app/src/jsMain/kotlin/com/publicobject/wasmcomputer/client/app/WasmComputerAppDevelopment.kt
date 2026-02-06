package com.publicobject.wasmcomputer.client.app

import kotlinx.browser.window

@JsExport
fun startOnLoad() {
  window.onload = {
    val app = WasmComputerApp()
    app.start()
  }
}
