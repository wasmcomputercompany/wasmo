@file:OptIn(
  ComponentModelInternalApi::class,
  ExperimentalWasmInterop::class,
  UnsafeWasmMemoryApi::class,
)

package com.wasmo.wasm

import kotlin.wasm.unsafe.ComponentModelInternalApi
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@WasmExport("printGreeting")
fun printGreeting(nameId: Int) {
  return withScopedMemoryAllocator { allocator ->
    val bridge = GuestBridge(allocator)
    val name = bridge.get(nameId)
    println("Hello, $name")
  }
}

@WasmExport("printError")
fun printError(nameId: Int) {
  return withScopedMemoryAllocator { allocator ->
    val bridge = GuestBridge(allocator)
    val name = bridge.get(nameId)
    val exception = Exception("boom, $name!")
    exception.printStackTrace()
  }
}
