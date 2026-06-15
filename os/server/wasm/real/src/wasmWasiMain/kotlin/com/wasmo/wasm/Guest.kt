package com.wasmo.wasm

import wit.wasmo.testing.WasmoTesting

lateinit var guest: WasmoTesting.Guest

@WasmExport("wasmo:testing#sum")
fun sum(a: Long, b: Long): Long {
  return guest.sum(a, b)
}
