package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.U_INT
import com.wasmo.support.wit.ir.IrTypeName
import com.wasmo.support.wit.ir.IrTypeNameDeclared
import kotlin.test.Test

class TypeMapperTest {
  @Test
  fun `map declared types`() {
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

  @Test
  fun `map built in types`() {
    val typeMapper = TypeMapper(kotlinPackagePrefix = "wit")

    assertThat(typeMapper.map(IrTypeName.U32)).isEqualTo(U_INT)
    assertThat(typeMapper.map(IrTypeName.List(IrTypeName.U32)))
      .isEqualTo(ClassName("kotlin", "UIntArray"))
    assertThat(typeMapper.map(IrTypeName.List(IrTypeName.String)))
      .isEqualTo(ClassName("kotlin.collections", "List").parameterizedBy(STRING))
  }
}
