@file:OptIn(
  ComponentModelInternalApi::class,
  ExperimentalWasmInterop::class,
  UnsafeWasmMemoryApi::class,
)

package com.wasmo.wasm

import kotlin.wasm.unsafe.ComponentModelInternalApi
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@WasmExport("concatenate")
fun concatenate(aId: Int, bId: Int): Int {
  return withScopedMemoryAllocator { allocator ->
    val bridge = GuestBridge(allocator)
    val a = bridge.get(aId)
    val b = bridge.get(bId)
    val concat = concatenator.concatenate(a, b)
    bridge.put(concat)
  }
}

lateinit var concatenator: Concatenator

interface Concatenator {
  fun concatenate(a: String, b: String): String
}
