package dev.wasmo.brevity.kotlin.generator

import com.squareup.kotlinpoet.ClassName

object Symbols {
  object KotlinWasm {
    val WasmExport = ClassName("kotlin.wasm", "WasmExport")
  }

  object ChicoryRuntime {
    val ExportFunction = ClassName("com.dylibso.chicory.runtime", "ExportFunction")
    val Instance = ClassName("com.dylibso.chicory.runtime", "Instance")
    val Store = ClassName("com.dylibso.chicory.runtime", "Store")
  }

  object Brevity {
    val World = ClassName("dev.wasmo.brevity", "World")
  }
}
