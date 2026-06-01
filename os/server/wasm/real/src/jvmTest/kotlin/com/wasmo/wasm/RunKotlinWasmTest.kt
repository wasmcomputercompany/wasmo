package com.wasmo.wasm

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNullOrEmpty
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath

class RunKotlinWasmTest {
  private val tester = WasmTester.Factory().createFromWasm(
    "build/compileSync/wasmWasi/main/developmentExecutable/kotlin/wasmo-os-server-wasm-real.wasm".toPath(),
  )

  @Test
  fun `call concatenate`() = runTest {
    val bId = tester.bridge.put("World!")
    val aId = tester.bridge.put("Hello, ")

    val concatenate = tester.instance.export("concatenate")
    val result = concatenate.apply(aId.toLong(), bId.toLong())

    assertThat(tester.bridge.get(result[0].toInt())).isEqualTo("Hello, World!")
  }

  @Test
  fun `call printGreeting`() = runTest {
    val nameId = tester.bridge.put("Jesse")

    val concatenate = tester.instance.export("printGreeting")
    val result = concatenate.apply(nameId.toLong())
    assertThat(result).isNullOrEmpty()

    assertThat(tester.wasi.stdout.readUtf8()).isEqualTo("Hello, Jesse\n")
  }

  @Test
  fun `call printError`() = runTest {
    val nameId = tester.bridge.put("Jesse")

    val concatenate = tester.instance.export("printError")
    val result = concatenate.apply(nameId.toLong())
    assertThat(result).isNullOrEmpty()

    assertThat(tester.wasi.stderr.readUtf8()).isEqualTo("Exception: boom, Jesse!\n\n")
  }
}
