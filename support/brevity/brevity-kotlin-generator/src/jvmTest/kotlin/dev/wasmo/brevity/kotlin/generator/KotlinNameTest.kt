package dev.wasmo.brevity.kotlin.generator

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.squareup.kotlinpoet.ClassName
import dev.wasmo.brevity.Identifier
import dev.wasmo.brevity.toPackageName
import kotlin.test.Test

class KotlinNameTest {
  @Test
  fun `package name mapping`() {
    val value = "wasi:clocks".toPackageName().toKotlin("wit")
    assertThat(value.name).isEqualTo("wit.wasi.clocks")
    assertThat((value + Identifier("wall-clock")).name)
      .isEqualTo(ClassName("wit.wasi.clocks", "WallClock"))
  }

  @Test
  fun `package name mapping with version`() {
    val value = "wasi:clocks@0.2.12".toPackageName().toKotlin("wit")
    assertThat(value.name).isEqualTo("wit.wasi.clocks.v0_2_12")
    assertThat((value + Identifier("wall-clock")).name)
      .isEqualTo(ClassName("wit.wasi.clocks.v0_2_12", "WallClock"))
  }
}
