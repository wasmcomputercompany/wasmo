package com.wasmo.wasm

import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.wabt.Wat2Wasm
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule
import okio.FileSystem
import okio.Path

class WasmTester(
  val store: Store,
  val bridge: HostBridge,
  val wasmModule: WasmModule,
  val instance: Instance,
  val wasi: FakeWasi,
) {
  class Factory {
    fun createFromWasm(path: Path): WasmTester {
      return FileSystem.SYSTEM.read(path) {
        create(readByteArray())
      }
    }

    fun createFromWat(wat: String): WasmTester {
      return create(Wat2Wasm.parse(wat))
    }

    fun create(wasmBytes: ByteArray): WasmTester {
      val wasmModule = Parser.parse(wasmBytes)
      val store = Store()
      val wasi = FakeWasi()
      val bridge = HostBridge(
        wasi = wasi,
      )
      satisfyImports(bridge, store)

      val instance = store.instantiate("name", wasmModule)

      return WasmTester(
        store = store,
        bridge = bridge,
        wasmModule = wasmModule,
        instance = instance,
        wasi = wasi,
      )
    }

    /** Provide the imports required to run our Kotlin/Wasm program. */
    private fun satisfyImports(bridge: HostBridge, store: Store) {
      bridge.addDataTransferFunctions(store)
      bridge.addWasiSnapshotPreview1Functions(store)
    }
  }
}

