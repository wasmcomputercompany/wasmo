package com.wasmo.wasm

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNullOrEmpty
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import wit.wasmo.testing.WasmoTesting
import wit.wasmo.testing.World

class RunKotlinWasmTest {
  @Test
  fun `call function declared on world`() = runTest {
    val world = WasmoTesting.World { Unit }
    val tester = WasmTester.Builder()
      .moduleWasm()
      .addWorld(world)
      .build()
    val result = world.guest.sum(5L, 10L)
    assertThat(result).isEqualTo(15L)
  }

  @Test
  fun `call function declared on interface`() = runTest {
    val world = WasmoTesting.World { Unit }
    val tester = WasmTester.Builder()
      .moduleWasm()
      .addWorld(world)
      .build()
    val result = world.guest.calculator.multiply(5L, 10L)
    assertThat(result).isEqualTo(50L)
  }

  @Test
  fun `call concatenate`() = runTest {
    val tester = WasmTester.Builder()
      .moduleWasm()
      .build()

    val bId = tester.bridge.put("World!")
    val aId = tester.bridge.put("Hello, ")

    val concatenate = tester.instance.export("concatenate")
    val result = concatenate.apply(aId.toLong(), bId.toLong())

    assertThat(tester.bridge.get(result[0].toInt())).isEqualTo("Hello, World!")
  }

  @Test
  fun `call printGreeting`() = runTest {
    val tester = WasmTester.Builder()
      .moduleWasm()
      .build()

    val nameId = tester.bridge.put("Jesse")

    val concatenate = tester.instance.export("printGreeting")
    val result = concatenate.apply(nameId.toLong())
    assertThat(result).isNullOrEmpty()

    assertThat(tester.wasi.stdout.readUtf8()).isEqualTo("Hello, Jesse\n")
  }

  @Test
  fun `call printError`() = runTest {
    val tester = WasmTester.Builder()
      .moduleWasm()
      .build()
    val nameId = tester.bridge.put("Jesse")

    val concatenate = tester.instance.export("printError")
    val result = concatenate.apply(nameId.toLong())
    assertThat(result).isNullOrEmpty()

    assertThat(tester.wasi.stderr.readUtf8()).isEqualTo("Exception: boom, Jesse!\n\n")
  }

  private fun WasmTester.Builder.moduleWasm() = apply {
    wasmPath("build/compileSync/wasmWasi/main/developmentExecutable/kotlin/brevity-brevity-host.wasm".toPath())
  }
}
