package com.wasmo.wasm

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.runtime.WasmFunctionHandle
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType

class HostBridge {
  private val values = mutableListOf<String>()

  fun addFunctions(store: Store) {
    store.addFunction(
      HostFunction(
        "bridge",
        "get",
        FunctionType.of(
          listOf(ValType.I32, ValType.I32),
          listOf(),
        ),
        WasmFunctionHandle { instance, args ->
          get(instance, args[0].toInt(), args[1].toInt())
          longArrayOf()
        },
      ),
    )
    store.addFunction(
      HostFunction(
        "bridge",
        "put",
        FunctionType.of(
          listOf(ValType.I32, ValType.I32),
          listOf(ValType.I32),
        ),
        WasmFunctionHandle { instance, args ->
          val result = put(instance, args[0].toInt(), args[1].toInt())
          longArrayOf(result.toLong())
        },
      ),
    )
  }

  fun put(string: String): Int {
    val id = values.size
    values += string
    return id
  }

  fun get(id: Int): String = values[id]

  /** Stores a string from the caller's memory and returns an ID. */
  private fun put(instance: Instance, address: Int, length: Int): Int {
    val memory = instance.memory()
    val bytes = memory.readBytes(address, length)
    val id = values.size
    values += bytes.decodeToString()
    return id
  }

  /** Loads a string into the caller's memory. Puts the pointer and location at [address]. */
  private fun get(instance: Instance, id: Int, address: Int) {
    val s = values[id].encodeToByteArray()
    val stringPointer = instance.cabiRealloc(0, 0, 1, s.size)
    val memory = instance.memory()
    memory.write(stringPointer, s)
    memory.writeI32(address, stringPointer)
    memory.writeI32(address + 4, s.size)
  }
}

internal fun Instance.cabiRealloc(
  oldPointer: Int,
  oldSize: Int,
  align: Int,
  newSize: Int,
): Int {
  val cabiRealloc = export("cabi_realloc")
  val result = cabiRealloc.apply(
    oldPointer.toLong(), oldSize.toLong(), align.toLong(), newSize.toLong(),
  )
  return result[0].toInt()
}
