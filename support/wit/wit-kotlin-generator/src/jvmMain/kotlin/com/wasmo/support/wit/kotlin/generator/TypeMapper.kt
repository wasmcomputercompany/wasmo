package com.wasmo.support.wit.kotlin.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName as KotlinTypeName
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.Scope
import com.wasmo.support.wit.SymbolIndex
import com.wasmo.support.wit.TypeName
import com.wasmo.support.wit.UsePath

/**
 * Map WIT types to Kotlin types.
 */
class TypeMapper(
  private val index: SymbolIndex,
  private val kotlinPackagePrefix: String,
  val scope: Scope,
) {
  fun withScope(interfaceName: Identifier) = TypeMapper(
    index = index,
    kotlinPackagePrefix = kotlinPackagePrefix,
    scope = scope.copy(interfaceName = interfaceName),
  )

  fun withScope(usePath: UsePath) = TypeMapper(
    index = index,
    kotlinPackagePrefix = kotlinPackagePrefix,
    scope = scope.copy(usePath = usePath),
  )

  fun map(typeName: TypeName): KotlinTypeName {
    return mapOrNull(typeName)
      ?: throw IllegalArgumentException("unable to find $typeName in $scope")
  }

  private fun mapOrNull(typeName: TypeName): KotlinTypeName? {
    val specialCase = ClassNames.WasmToKotlin[typeName]
    if (specialCase != null) return specialCase

    return when (typeName) {
      TypeName.Bool -> ClassNames.Boolean
      TypeName.S8 -> ClassNames.Byte
      TypeName.S16 -> ClassNames.Short
      TypeName.S32 -> ClassNames.Int
      TypeName.S64 -> ClassNames.Long
      TypeName.U8 -> ClassNames.UByte
      TypeName.U16 -> ClassNames.UShort
      TypeName.U32 -> ClassNames.UInt
      TypeName.U64 -> ClassNames.ULong
      TypeName.F32 -> ClassNames.Float
      TypeName.F64 -> ClassNames.Double
      TypeName.Char -> ClassNames.Int
      TypeName.String -> ClassNames.String

      is TypeName.Borrow -> ClassNames.Borrow.parameterizedBy(map(typeName.type))
      is TypeName.Future -> ClassNames.Deferred.parameterizedBy(
        typeName.type?.let { map(it) } ?: STAR,
      )

      is TypeName.List -> ClassNames.List.parameterizedBy(map(typeName.type))
      is TypeName.Map -> ClassNames.Map.parameterizedBy(
        map(typeName.key),
        map(typeName.value),
      )

      is TypeName.Option -> map(typeName.type).copy(nullable = true)
      is TypeName.Result -> ClassNames.Pair.parameterizedBy(
        typeName.ok?.let { map(it) } ?: STAR,
        typeName.err?.let { map(it) } ?: STAR,
      )

      is TypeName.Declared -> className(
        packagePrefix = kotlinPackagePrefix,
        scope = scope,
        typeName = index.getType(scope, typeName).typeName,
      )

      is TypeName.Stream -> ClassNames.Stream.parameterizedBy(
        typeName.type?.let { map(it) } ?: STAR,
      )

      is TypeName.Tuple -> {
        val typeArguments = typeName.types.map { map(it) }
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

private object ClassNames {
  val Boolean = ClassName("kotlin", "Boolean")
  val Borrow = ClassName("com.wasmo.support.wit.kotlin", "Borrow")
  val Byte = ClassName("kotlin", "Byte")
  val Deferred = ClassName("kotlinx.coroutines", "Deferred")
  val Double = ClassName("kotlin", "Double")
  val Float = ClassName("kotlin", "Float")
  val Int = ClassName("kotlin", "Int")
  val List = ClassName("kotlin.collections", "List")
  val Long = ClassName("kotlin", "Long")
  val Map = ClassName("kotlin.collections", "Map")
  val Pair = ClassName("kotlin", "Pair")
  val Quad = ClassName("com.wasmo.support.wit.kotlin", "Quad")
  val Short = ClassName("kotlin", "Short")
  val Stream = ClassName("com.wasmo.support.wit.kotlin", "Stream")
  val String = ClassName("kotlin", "String")
  val Triple = ClassName("kotlin", "Triple")
  val UByte = ClassName("kotlin", "UByte")
  val UInt = ClassName("kotlin", "UInt")
  val ULong = ClassName("kotlin", "ULong")
  val UShort = ClassName("kotlin", "UShort")

  val WasmToKotlin = mapOf(
    TypeName.List(TypeName.S8) to ClassName("kotlin", "ByteArray"),
    TypeName.List(TypeName.S16) to ClassName("kotlin", "ShortArray"),
    TypeName.List(TypeName.S32) to ClassName("kotlin", "IntArray"),
    TypeName.List(TypeName.S64) to ClassName("kotlin", "LongAray"),
    TypeName.List(TypeName.U8) to ClassName("kotlin", "UByteArray"),
    TypeName.List(TypeName.U16) to ClassName("kotlin", "UShortArray"),
    TypeName.List(TypeName.U32) to ClassName("kotlin", "UIntArray"),
    TypeName.List(TypeName.U64) to ClassName("kotlin", "ULongAray"),
    TypeName.List(TypeName.F32) to ClassName("kotlin", "FloatArray"),
    TypeName.List(TypeName.F64) to ClassName("kotlin", "DoubleArray"),
  )
}
