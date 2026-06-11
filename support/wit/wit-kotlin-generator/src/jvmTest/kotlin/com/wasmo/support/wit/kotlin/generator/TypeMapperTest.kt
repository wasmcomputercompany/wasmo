package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.wasmo.support.wit.io.IrTypeNameDeclared
import com.wasmo.support.wit.ir.IrTypeName
import kotlin.test.Test

class TypeMapperTest {
  @Test
  fun `map types`() {
    val typeMapper = TypeMapper(kotlinPackagePrefix = "wit")

    assertThat(
      typeMapper.map(IrTypeNameDeclared("wasi:clocks", "wall-clock", "datetime")),
    ).isEqualTo(ClassName("wit.wasi.clocks", "WallClock", "Datetime"))

    assertThat(
      typeMapper.map(IrTypeName.List(IrTypeNameDeclared("wasi:clocks", "wall-clock", "datetime"))),
    ).isEqualTo(
      ClassName("kotlin.collections", "List")
        .parameterizedBy(ClassName("wit.wasi.clocks", "WallClock", "Datetime")),
    )
  }
}
