package com.wasmo.support.wit.kotlin.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName as KotlinTypeName
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.TypeName
import com.wasmo.support.wit.Types
import com.wasmo.support.wit.WitFile

/**
 * Map WIT types to Kotlin types.
 */
class TypeResolver(
  private val kotlinPackageName: String,
  private val witFile: WitFile,
) {
  inner class Scope(
    val packageName: PackageName? = null,
    val interfaceName: Identifier? = null,
  ) {
    val typeName: ClassName
      get() {
        val name = interfaceName?.name ?: error("unexpected call to typeName")
        return ClassName(kotlinPackageName, name)
      }

    fun copy(interfaceName: Identifier) = Scope(packageName, interfaceName)

    fun resolveTypeName(typeName: TypeName): KotlinTypeName {
      val specialCase = ClassNames.WasmToKotlin[typeName]
      if (specialCase != null) return specialCase

      return when (typeName) {
        is TypeName.Borrow -> ClassNames.Borrow.parameterizedBy(resolveTypeName(typeName.type))
        is TypeName.Future -> ClassNames.Deferred.parameterizedBy(
          typeName.type?.let { resolveTypeName(it) } ?: STAR,
        )

        is TypeName.List -> ClassNames.List.parameterizedBy(resolveTypeName(typeName.type))
        is TypeName.Map -> ClassNames.Map.parameterizedBy(
          resolveTypeName(typeName.key),
          resolveTypeName(typeName.value),
        )

        is TypeName.Option -> resolveTypeName(typeName.type).copy(nullable = true)
        is TypeName.Result -> ClassNames.Pair.parameterizedBy(
          typeName.ok?.let { resolveTypeName(it) } ?: STAR,
          typeName.err?.let { resolveTypeName(it) } ?: STAR,
        )

        is TypeName.Simple -> STRING

        is TypeName.Stream -> ClassNames.Stream.parameterizedBy(
          typeName.type?.let { resolveTypeName(it) } ?: STAR,
        )

        is TypeName.Tuple -> {
          val typeArguments = typeName.types.map { resolveTypeName(it) }
          when (typeArguments.size) {
            2 -> ClassNames.Pair.parameterizedBy(typeArguments)
            3 -> ClassNames.Triple.parameterizedBy(typeArguments)
            4 -> ClassNames.Quad.parameterizedBy(typeArguments)
            else -> ClassNames.List.parameterizedBy(STAR)
          }
        }
      }
    }
  }
}

private object ClassNames {
  val Borrow = ClassName("com.wasmo.support.wit.kotlin", "Borrow")
  val Deferred = ClassName("kotlinx.coroutines", "Deferred")
  val List = ClassName("kotlin.collections", "List")
  val Map = ClassName("kotlin.collections", "Map")
  val Pair = ClassName("kotlin", "Pair")
  val Triple = ClassName("kotlin", "Triple")
  val Quad = ClassName("com.wasmo.support.wit.kotlin", "Quad")
  val Stream = ClassName("com.wasmo.support.wit.kotlin", "Stream")

  val WasmToKotlin = mapOf(
    Types.bool to ClassName("kotlin", "Boolean"),
    Types.s8 to ClassName("kotlin", "Byte"),
    Types.s16 to ClassName("kotlin", "Short"),
    Types.s32 to ClassName("kotlin", "Int"),
    Types.s64 to ClassName("kotlin", "Long"),
    Types.u8 to ClassName("kotlin", "UByte"),
    Types.u16 to ClassName("kotlin", "UShort"),
    Types.u32 to ClassName("kotlin", "UInt"),
    Types.u64 to ClassName("kotlin", "ULong"),
    Types.f32 to ClassName("kotlin", "Float"),
    Types.f64 to ClassName("kotlin", "Double"),
    Types.char to ClassName("kotlin", "Int"),
    Types.string to ClassName("kotlin", "String"),
    TypeName.List(Types.s8) to ClassName("kotlin", "ByteArray"),
    TypeName.List(Types.s16) to ClassName("kotlin", "ShortArray"),
    TypeName.List(Types.s32) to ClassName("kotlin", "IntArray"),
    TypeName.List(Types.s64) to ClassName("kotlin", "LongAray"),
    TypeName.List(Types.u8) to ClassName("kotlin", "UByteArray"),
    TypeName.List(Types.u16) to ClassName("kotlin", "UShortArray"),
    TypeName.List(Types.u32) to ClassName("kotlin", "UIntArray"),
    TypeName.List(Types.u64) to ClassName("kotlin", "ULongAray"),
    TypeName.List(Types.f32) to ClassName("kotlin", "FloatArray"),
    TypeName.List(Types.f64) to ClassName("kotlin", "DoubleArray"),
  )
}
