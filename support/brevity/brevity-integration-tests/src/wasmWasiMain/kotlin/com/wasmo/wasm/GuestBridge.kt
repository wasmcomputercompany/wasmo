@file:OptIn(
  ComponentModelInternalApi::class,
  ExperimentalWasmInterop::class,
  UnsafeWasmMemoryApi::class,
)

package com.wasmo.wasm

import kotlin.wasm.unsafe.ComponentModelInternalApi
import kotlin.wasm.unsafe.MemoryAllocator
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.componentModelRealloc
import kotlin.wasm.unsafe.freeAllComponentModelReallocAllocatedMemory

class GuestBridge(
  val memoryAllocator: MemoryAllocator,
) {
  fun get(id: Int): String {
    val pointerAndLength = memoryAllocator.allocate(8)
    get(id, pointerAndLength.address.toInt())
    val result = get(
      Pointer(pointerAndLength.loadInt().toUInt()),
      (pointerAndLength + 4U).loadInt(),
    )
    freeAllComponentModelReallocAllocatedMemory()
    return result
  }

  fun put(value: String): Int {
    val byteArray = value.encodeToByteArray()
    val address = memoryPut(byteArray)
    return put(address.address.toInt(), byteArray.size)
  }

  private fun get(addr: Pointer, size: Int): String =
    memoryGet(addr, size).decodeToString()

  private fun memoryGet(address: Pointer, size: Int): ByteArray =
    ByteArray(size) { i -> (address + i).loadByte() }

  private fun memoryPut(array: ByteArray): Pointer {
    val address = memoryAllocator.allocate(array.size)
    var p = address
    for (b in array) {
      p.storeByte(b)
      p += 1
    }
    return address
  }
}

@WasmExport(name = "cabi_realloc")
fun cabi_realloc(ptr: Int, oldSize: Int, align: Int, newSize: Int): Int =
  componentModelRealloc(ptr, oldSize, newSize)

/** Loads bytes into the caller's memory. Puts the data's address and location at [address]. */
@WasmImport("bridge", "get")
external fun get(id: Int, address: Int)

/** Stores bytes from the caller's memory and returns an ID. */
@WasmImport("bridge", "put")
private external fun put(ptr: Int, length: Int): Int

