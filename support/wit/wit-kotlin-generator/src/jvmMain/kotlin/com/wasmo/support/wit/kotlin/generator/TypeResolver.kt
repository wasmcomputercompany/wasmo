package com.wasmo.support.wit.kotlin.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName as KotlinTypeName
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.Interface
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.TypeDeclaration
import com.wasmo.support.wit.TypeName
import com.wasmo.support.wit.Types
import com.wasmo.support.wit.WitFile

/**
 * Map WIT types to Kotlin types.
 */
sealed interface TypeResolver {
  fun resolveTypeName(typeName: TypeName): KotlinTypeName
}

interface RootTypeResolver : TypeResolver {
  fun refine(packageName: PackageName?): PackageTypeResolver
}

interface PackageTypeResolver : TypeResolver {
  val packageName: PackageName?
  fun refine(interfaceName: Identifier): InterfaceTypeResolver
}

interface InterfaceTypeResolver : PackageTypeResolver {
  val interfaceName: Identifier
  val className: ClassName
}

fun TypeResolver(
  witFiles: List<WitFile>,
  kotlinPackageName: String,
): RootTypeResolver = RealRootTypeResolver(
  witFiles = witFiles,
  kotlinPackageName = kotlinPackageName,
)

internal class RealRootTypeResolver(
  private val witFiles: List<WitFile>,
  private val kotlinPackageName: String,
) : RootTypeResolver {
  override fun resolveTypeName(typeName: TypeName): KotlinTypeName =
    resolveTypeNameOrNull(typeName)
      ?: error("unable to resolve $typeName")

  private fun resolveSimpleTypeNameOrNull(
    typeName: TypeName.Simple,
    packageName: PackageName? = null,
    interfaceName: Identifier? = null,
  ): KotlinTypeName? {
    for (witFile in witFiles) {
      if (witFile.packageName != packageName) continue

      for (`interface` in witFile.declarations) {
        if (`interface` !is Interface || `interface`.name != interfaceName) continue

        for (type in `interface`.declarations) {
          if (type !is TypeDeclaration) continue
          if (type.name == typeName.name) {
            return className(witFile.packageName, interfaceName, type.name)
          }
        }
      }
    }

    return null
  }

  private fun resolveTypeNameOrNull(
    typeName: TypeName,
    packageName: PackageName? = null,
    interfaceName: Identifier? = null,
  ): KotlinTypeName? {
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

      is TypeName.Simple -> resolveSimpleTypeNameOrNull(typeName, packageName, interfaceName)

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

  override fun refine(packageName: PackageName?) = RealPackageTypeResolver(packageName)

  inner class RealPackageTypeResolver internal constructor(
    override val packageName: PackageName?,
  ) : PackageTypeResolver {
    override fun refine(interfaceName: Identifier) =
      RealInterfaceTypeResolver(packageName, interfaceName)

    override fun resolveTypeName(typeName: TypeName): KotlinTypeName {
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

  inner class RealInterfaceTypeResolver internal constructor(
    override val packageName: PackageName?,
    override val interfaceName: Identifier,
  ) : InterfaceTypeResolver {
    override val className: ClassName
      get() = className(packageName, interfaceName)

    override fun refine(interfaceName: Identifier) =
      RealInterfaceTypeResolver(packageName, interfaceName)

    override fun resolveTypeName(typeName: TypeName): KotlinTypeName {
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
