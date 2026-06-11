package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.squareup.kotlinpoet.ClassName
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.io.toPackageName
import kotlin.test.Test

class NameMapperTest {
  @Test
  fun `package name mapping`() {
    val nameMapper = "wasi:clocks".toPackageName().toNameMapper("wit")
    assertThat(nameMapper.packageName).isEqualTo("wit.wasi.clocks")
    assertThat((nameMapper + Identifier("wall-clock")).className)
      .isEqualTo(ClassName("wit.wasi.clocks", "WallClock"))
  }

  @Test
  fun `package name mapping with version`() {
    val nameMapper = "wasi:clocks@0.2.12".toPackageName().toNameMapper("wit")
    assertThat(nameMapper.packageName).isEqualTo("wit.wasi.clocks.v0_2_12")
    assertThat((nameMapper + Identifier("wall-clock")).className)
      .isEqualTo(ClassName("wit.wasi.clocks.v0_2_12", "WallClock"))
  }
}
