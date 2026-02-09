package com.wasmo.wasm

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.testing.WatCompiler
import kotlin.test.Test
import okio.ByteString
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.io.ByteSequence


class WasmRunnerTest {
  @Test
  fun test() {
    val wasmBytes = WatCompiler().compile(
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

    Context.create("wasm").use { context ->
      val source = Source.newBuilder("wasm", wasmBytes.sequence, "addTwo").build()
      val mainModule = context.eval(source)
      val mainInstance = mainModule.newInstance()
      val addTwo = mainInstance.getMember("exports").getMember("addTwo")
      val result = addTwo.execute(40, 2)
      assertThat(result.asInt()).isEqualTo(42)
    }
  }
}

private val ByteString.sequence: ByteSequence
  get() = object : ByteSequence {
    override fun length() = this@sequence.size
    override fun byteAt(index: Int) = this@sequence[index]
  }
