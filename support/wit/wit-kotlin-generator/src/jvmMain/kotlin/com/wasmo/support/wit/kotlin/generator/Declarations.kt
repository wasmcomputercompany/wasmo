package com.wasmo.support.wit.kotlin.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

sealed interface DeclarationKt {
  val documentation: String?
}

data class WitPackageKt(
  val packageName: String,
  val declarations: List<DeclarationKt>,
)

data class InterfaceKt(
  override val documentation: String?,
  val type: ClassName,
  val declarations: List<DeclarationKt>,
) : DeclarationKt

data class WorldKt(
  override val documentation: String?,
  val type: ClassName,
  val imports : List<Import>,
  val exports : List<Export>,
) : DeclarationKt {
  data class Import(
    override val documentation: String?,
    val name: String,
    val type: TypeName,
  ): DeclarationKt
  data class Export(
    override val documentation: String?,
    val name: String,
    val type: TypeName,
  ): DeclarationKt
}

data class EnumKt(
  override val documentation: String?,
  val type: ClassName,
  val cases: List<Case>,
) : DeclarationKt {
  data class Case(
    override val documentation: String?,
    val name: String,
  ) : DeclarationKt
}

data class RecordKt(
  override val documentation: String?,
  val type: ClassName,
  val fields: List<Field>,
) : DeclarationKt {
  data class Field(
    override val documentation: String?,
    val name: String,
    val type: TypeName,
  ) : DeclarationKt
}

data class ResourceKt(
  override val documentation: String?,
  val type: ClassName,
  val functions: List<FunctionKt>,
) : DeclarationKt

data class TypeAliasKt(
  override val documentation: String?,
  val type: ClassName,
  val target: TypeName
) : DeclarationKt

data class VariantKt(
  override val documentation: String?,
  val type: ClassName,
  val cases: List<Case>,
) : DeclarationKt {
  data class Case(
    override val documentation: String?,
    val name: String,
    val type: TypeName?,
  ) : DeclarationKt
}

data class FlagsKt(
  override val documentation: String?,
  val type: ClassName,
  val flags: List<Flag>,
) : DeclarationKt {
  data class Flag(
    override val documentation: String?,
    val name: String,
  ) : DeclarationKt
}

data class FunctionKt(
  override val documentation: String?,
  val name: String,
  val parameters: List<Parameter>,
  val returnType: TypeName?,
) : DeclarationKt {
  data class Parameter(
    override val documentation: String?,
    val name: String,
    val type: TypeName,
  ) : DeclarationKt
}
