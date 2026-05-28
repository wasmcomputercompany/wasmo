package com.wasmo.wasm

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.runtime.WasmFunctionHandle
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType
import kotlin.test.Test
import okio.FileSystem
import okio.Path.Companion.toPath

class RunKotlinWasmTest {
  val wasmPath =
    "build/compileSync/wasmWasi/main/developmentExecutable/kotlin/wasmo-os-server-wasm-real.wasm".toPath()

  @Test
  fun `run kotlin wasm`() {
    val wasmBytes = FileSystem.SYSTEM.read(wasmPath) {
      readByteArray()
    }

    val wasmModule = Parser.parse(wasmBytes)

    val store = Store()
    val bridge = HostBridge()
    satisfyImports(wasmModule, store, bridge)

    val instance = store.instantiate("addTwo", wasmModule)

    val bId = bridge.put("World!")
    val aId = bridge.put("Hello, ")

    val concatenate = instance.export("concatenate")
    val result = concatenate.apply(aId.toLong(), bId.toLong())

    assertThat(bridge.get(result[0].toInt())).isEqualTo("Hello, World!")
  }

  /** Provide the imports required to run our Kotlin/Wasm program. */
  private fun satisfyImports(wasmModule: WasmModule, store: Store, hostBridge: HostBridge) {
    val imports = wasmModule.importSection().stream().toList()
    val randomGetImport = imports.single {
      it.module() == "wasi_snapshot_preview1" && it.name() == "random_get"
    }
    check(randomGetImport != null)

    // KT-82105: Kotlin/Wasm binaries always import random_get.
    store.addFunction(
      HostFunction(
        "wasi_snapshot_preview1",
        "random_get",
        FunctionType.of(
          listOf(ValType.I32, ValType.I32),
          listOf(ValType.I32),
        ),
        WasmFunctionHandle { instance, args ->
          error("unexpected call")
        },
      ),
    )
    hostBridge.addFunctions(store)
  }
}
