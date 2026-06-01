package com.wasmo.wasm

import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

class RunWatTest {
  @Test
  fun `run wat`() {
    val tester = WasmTester.Factory().createFromWat(
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

    val addTwo = tester.instance.export("addTwo")
    val result = addTwo.apply(40, 2)
    assertThat(result).containsExactly(42L)
  }
}
