@file:OptIn(ExperimentalWasmInterop::class)

package com.wasmo.wasm

@WasmExport("addTwo")
fun addTwo(a: Int, b: Int): Int {
  return a + b
}
