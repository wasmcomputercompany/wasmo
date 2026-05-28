package com.wasmo.wasm

import assertk.assertThat
import assertk.assertions.containsExactly
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wabt.Wat2Wasm
import com.dylibso.chicory.wasm.Parser
import kotlin.test.Test

class RunWatTest {
  @Test
  fun `run wat`() {
    val wasmBytes = Wat2Wasm.parse(
      """
      (module
        (func (export "addTwo") (param i32 i32) (result i32)
          local.get 0
          local.get 1
          i32.add
        )
      )
      """.trimIndent(),
    )

    val module = Parser.parse(wasmBytes)
    val instance = Instance.builder(module).build()
    val addTwo = instance.export("addTwo")
    val result = addTwo.apply(40, 2)
    assertThat(result).containsExactly(42L)
  }
}
