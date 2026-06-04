package com.wasmo.support.wit.kotlin.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName as KotlinTypeName
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.SymbolResolver
import com.wasmo.support.wit.TypeName
import com.wasmo.support.wit.Types

/**
 * Map WIT types to Kotlin types.
 */
sealed interface TypeMapper {
  fun map(typeName: TypeName): KotlinTypeName
}

interface RootTypeMapper : TypeMapper {
  fun refine(packageName: PackageName?): PackageTypeMapper
}

interface PackageTypeMapper : TypeMapper {
  val packageName: PackageName?
  fun refine(interfaceName: Identifier): InterfaceTypeMapper
}

interface InterfaceTypeMapper : PackageTypeMapper {
  val interfaceName: Identifier
  val className: ClassName
}

fun TypeMapper(
  symbolResolver: SymbolResolver,
  kotlinPackageName: String,
): RootTypeMapper = RealRootTypeMapper(
  symbolResolver = symbolResolver,
  kotlinPackageName = kotlinPackageName,
)

internal class RealRootTypeMapper(
  private val symbolResolver: SymbolResolver,
  private val kotlinPackageName: String,
) : RootTypeMapper {
  override fun map(typeName: TypeName): KotlinTypeName =
    resolveTypeNameOrNull(typeName)
      ?: error("unable to resolve $typeName")

  private fun resolveSimpleTypeNameOrNull(
    typeName: TypeName.Simple,
    packageName: PackageName? = null,
    interfaceName: Identifier? = null,
  ): ClassName {
    val typePath = symbolResolver.resolveType(typeName, packageName, interfaceName)
    return className(typePath.packageName, typePath.interfaceName, typePath.typeName)
  }

  private fun resolveTypeNameOrNull(
    typeName: TypeName,
    packageName: PackageName? = null,
    interfaceName: Identifier? = null,
  ): KotlinTypeName? {
    val specialCase = ClassNames.WasmToKotlin[typeName]
    if (specialCase != null) return specialCase

    return when (typeName) {
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

      is TypeName.Simple -> resolveSimpleTypeNameOrNull(typeName, packageName, interfaceName)

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

  override fun refine(packageName: PackageName?) = RealPackageTypeMapper(packageName)

  inner class RealPackageTypeMapper internal constructor(
    override val packageName: PackageName?,
  ) : PackageTypeMapper {
    override fun refine(interfaceName: Identifier) =
      RealInterfaceTypeMapper(packageName, interfaceName)

    override fun map(typeName: TypeName): KotlinTypeName {
      return resolveTypeNameOrNull(typeName, packageName)
        ?: throw IllegalArgumentException(
          buildString {
            append("unable to resolve $typeName")
            if (packageName != null) {
              append(" in $packageName")
            }
          },
        )
    }
  }

  inner class RealInterfaceTypeMapper internal constructor(
    override val packageName: PackageName?,
    override val interfaceName: Identifier,
  ) : InterfaceTypeMapper {
    override val className: ClassName
      get() = className(packageName, interfaceName)

    override fun refine(interfaceName: Identifier) =
      RealInterfaceTypeMapper(packageName, interfaceName)

    override fun map(typeName: TypeName): KotlinTypeName {
      return resolveTypeNameOrNull(typeName, packageName, interfaceName)
        ?: throw IllegalArgumentException(
          buildString {
            append("unable to resolve $typeName")
            if (packageName != null) {
              append(" in $packageName.$interfaceName")
            } else {
              append(" in $interfaceName")
            }
          },
        )
    }
  }

  private fun className(
    packageName: PackageName?,
    interfaceName: Identifier,
  ) = ClassName(kotlinPackageName, interfaceName.name)

  private fun className(
    packageName: PackageName?,
    interfaceName: Identifier,
    typeName: Identifier,
  ) = className(packageName, interfaceName).nestedClass(typeName.name)
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
