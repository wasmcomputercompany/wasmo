package dev.wasmo.brevity.kotlin.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName as KotlinTypeName
import dev.wasmo.brevity.ir.IrParentName
import dev.wasmo.brevity.ir.IrTypeName

/**
 * Map WIT types to Kotlin types.
 */
class TypeMapper(
  private val kotlinPackagePrefix: String,
) {
  fun map(typeName: IrParentName): KotlinTypeName {
    return (typeName.packageName.toKotlin(kotlinPackagePrefix) + typeName.name).name
  }

  fun map(typeName: IrTypeName): KotlinTypeName {
    val specialCase = ClassNames.WasmToKotlin[typeName]
    if (specialCase != null) return specialCase

    return when (typeName) {
      IrTypeName.Bool -> ClassNames.Boolean
      IrTypeName.S8 -> ClassNames.Byte
      IrTypeName.S16 -> ClassNames.Short
      IrTypeName.S32 -> ClassNames.Int
      IrTypeName.S64 -> ClassNames.Long
      IrTypeName.U8 -> ClassNames.UByte
      IrTypeName.U16 -> ClassNames.UShort
      IrTypeName.U32 -> ClassNames.UInt
      IrTypeName.U64 -> ClassNames.ULong
      IrTypeName.F32 -> ClassNames.Float
      IrTypeName.F64 -> ClassNames.Double
      IrTypeName.Char -> ClassNames.Int
      IrTypeName.String -> ClassNames.String

      is IrTypeName.Borrow -> ClassNames.Borrow.parameterizedBy(map(typeName.type))
      is IrTypeName.Future -> ClassNames.Deferred.parameterizedBy(
        typeName.type?.let { map(it) } ?: STAR,
      )

      is IrTypeName.List -> ClassNames.List.parameterizedBy(map(typeName.type))
      is IrTypeName.Map -> ClassNames.Map.parameterizedBy(
        map(typeName.key),
        map(typeName.value),
      )

      is IrTypeName.Option -> map(typeName.type).copy(nullable = true)
      is IrTypeName.Result -> ClassNames.Pair.parameterizedBy(
        typeName.ok?.let { map(it) } ?: STAR,
        typeName.err?.let { map(it) } ?: STAR,
      )

      is IrTypeName.Declared -> {
        typeName.toKotlin(kotlinPackagePrefix).name
      }

      is IrTypeName.Stream -> ClassNames.Stream.parameterizedBy(
        typeName.type?.let { map(it) } ?: STAR,
      )

      is IrTypeName.Tuple -> {
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
  val Borrow = ClassName("dev.wasmo.brevity", "Borrow")
  val Byte = ClassName("kotlin", "Byte")
  val Deferred = ClassName("kotlinx.coroutines", "Deferred")
  val Double = ClassName("kotlin", "Double")
  val Float = ClassName("kotlin", "Float")
  val Int = ClassName("kotlin", "Int")
  val List = ClassName("kotlin.collections", "List")
  val Long = ClassName("kotlin", "Long")
  val Map = ClassName("kotlin.collections", "Map")
  val Pair = ClassName("kotlin", "Pair")
  val Quad = ClassName("dev.wasmo.brevity", "Quad")
  val Short = ClassName("kotlin", "Short")
  val Stream = ClassName("dev.wasmo.brevity", "Stream")
  val String = ClassName("kotlin", "String")
  val Triple = ClassName("kotlin", "Triple")
  val UByte = ClassName("kotlin", "UByte")
  val UInt = ClassName("kotlin", "UInt")
  val ULong = ClassName("kotlin", "ULong")
  val UShort = ClassName("kotlin", "UShort")

  val WasmToKotlin = mapOf(
    IrTypeName.List(IrTypeName.S8) to ClassName("kotlin", "ByteArray"),
    IrTypeName.List(IrTypeName.S16) to ClassName("kotlin", "ShortArray"),
    IrTypeName.List(IrTypeName.S32) to ClassName("kotlin", "IntArray"),
    IrTypeName.List(IrTypeName.S64) to ClassName("kotlin", "LongAray"),
    IrTypeName.List(IrTypeName.U8) to ClassName("kotlin", "UByteArray"),
    IrTypeName.List(IrTypeName.U16) to ClassName("kotlin", "UShortArray"),
    IrTypeName.List(IrTypeName.U32) to ClassName("kotlin", "UIntArray"),
    IrTypeName.List(IrTypeName.U64) to ClassName("kotlin", "ULongAray"),
    IrTypeName.List(IrTypeName.F32) to ClassName("kotlin", "FloatArray"),
    IrTypeName.List(IrTypeName.F64) to ClassName("kotlin", "DoubleArray"),
  )
}
