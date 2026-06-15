package com.wasmo.wasm

import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.wabt.Wat2Wasm
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule
import dev.wasmo.brevity.World
import okio.FileSystem
import okio.Path

class WasmTester(
  val store: Store,
  val bridge: HostBridge,
  val wasmModule: WasmModule,
  val instance: Instance,
  val wasi: FakeWasi,
) {
  class Builder {
    private var wasmBytes: ByteArray? = null
    private val worlds = mutableListOf<World<*, *>>()

    fun wasmPath(path: Path) = apply {
      wasmBytes = FileSystem.SYSTEM.read(path) {
        readByteArray()
      }
    }

    fun wat(wat: String) = apply {
      wasmBytes = Wat2Wasm.parse(wat)
    }

    fun addWorld(world: World<*, *>) = apply {
      worlds += world
    }

    fun build(): WasmTester {
      check(wasmBytes != null) { "call wasmPath() or wat() first" }
      val wasmModule = Parser.parse(wasmBytes)
      val store = Store()
      val wasi = FakeWasi()
      val bridge = HostBridge(
        wasi = wasi,
      )
      satisfyImports(bridge, store)
      for (world in worlds) {
        world.initImports(store)
      }

      val instance = store.instantiate("name", wasmModule)
      for (world in worlds) {
        world.initExports(instance)
      }

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

