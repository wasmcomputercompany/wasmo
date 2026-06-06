package com.wasmo.support.wit.kotlin.generator

import com.wasmo.support.wit.Case
import com.wasmo.support.wit.Declaration
import com.wasmo.support.wit.Enum
import com.wasmo.support.wit.Export
import com.wasmo.support.wit.ExternalUsePath
import com.wasmo.support.wit.Field
import com.wasmo.support.wit.Flag
import com.wasmo.support.wit.Flags
import com.wasmo.support.wit.Function
import com.wasmo.support.wit.Import
import com.wasmo.support.wit.Include
import com.wasmo.support.wit.Interface
import com.wasmo.support.wit.Package
import com.wasmo.support.wit.Record
import com.wasmo.support.wit.Resource
import com.wasmo.support.wit.SymbolResolver
import com.wasmo.support.wit.TopLevelUse
import com.wasmo.support.wit.TypeAlias
import com.wasmo.support.wit.Use
import com.wasmo.support.wit.Variant
import com.wasmo.support.wit.WitPackage
import com.wasmo.support.wit.World

/**
 * Directly converts WIT model types ([World], [Function], etc.) to a Kotlin equivalents ([WorldKt],
 * [FunctionKt], etc.). This implements case mapping (`monotonic-clock` to `MonotonicClock`) and
 * type mapping (`s32` to `Int`).
 */
class KotlinMapper(
  private val witPackages: List<WitPackage>,
  private val kotlinPackagePrefix: String = "wit",
) {
  fun mapPackage(witPackage: WitPackage): WitPackageKt {
    val typeMapper = TypeMapper(
      symbolResolver = SymbolResolver(witPackages),
      kotlinPackagePrefix = kotlinPackagePrefix,
    )

    return WitPackageKt(
      packageName = witPackage.packageName?.toKotlin(kotlinPackagePrefix) ?: kotlinPackagePrefix,
      declarations = witPackage.files.values.flatMap { witFile ->
        witFile.declarations.mapNotNull {
          mapDeclaration(
            typeMapper = typeMapper.refine(witPackage.packageName),
            value = it,
          )
        }
      },
    )
  }

  private fun mapDeclaration(typeMapper: PackageTypeMapper, value: Declaration): DeclarationKt? {
    return when (value) {
      is Case -> error("unexpected call")
      is Enum -> mapEnum(typeMapper as InterfaceTypeMapper, value)
      is Export -> null
      is Field -> error("unexpected call")
      is Flag -> error("unexpected call")
      is Flags -> mapFlags(typeMapper as InterfaceTypeMapper, value)
      is Function -> mapFunction(typeMapper, value)
      is Import -> null
      is Include -> null
      is Interface -> mapInterface(typeMapper, value)
      is Package -> null
      is Record -> mapRecord(typeMapper as InterfaceTypeMapper, value)
      is Resource -> mapResource(typeMapper as InterfaceTypeMapper, value)
      is TopLevelUse -> null
      is TypeAlias -> null
      is Use -> null
      is Variant -> mapVariant(typeMapper as InterfaceTypeMapper, value)
      is World -> mapWorld(typeMapper, value)
    }
  }

  fun mapInterface(typeMapper: PackageTypeMapper, value: Interface): InterfaceKt {
    val interfaceTypeMapper = typeMapper.refine(interfaceName = value.name)
    return InterfaceKt(
      documentation = value.documentation?.content,
      type = interfaceTypeMapper.className,
      declarations = value.declarations.mapNotNull {
        mapDeclaration(interfaceTypeMapper, it)
      },
    )
  }

  fun mapRecord(typeMapper: InterfaceTypeMapper, value: Record) = RecordKt(
    documentation = value.documentation?.content,
    type = typeMapper.className.nestedClass(value.name.name.toCamelCase(upperCamel = true)),
    fields = value.fields.map { field ->
      RecordKt.Field(
        documentation = field.documentation?.content,
        name = field.name.name.toCamelCase(upperCamel = false),
        type = typeMapper.map(field.type),
      )
    },
  )

  fun mapResource(typeMapper: InterfaceTypeMapper, value: Resource) = ResourceKt(
    documentation = value.documentation?.content,
    type = typeMapper.className.nestedClass(value.name.name.toCamelCase(upperCamel = true)),
    functions = value.functions.map { mapFunction(typeMapper, it) },
  )

  fun mapVariant(typeMapper: InterfaceTypeMapper, value: Variant) = VariantKt(
    documentation = value.documentation?.content,
    type = typeMapper.className.nestedClass(value.name.name.toCamelCase(upperCamel = true)),
    cases = value.cases.map { case ->
      VariantKt.Case(
        documentation = case.documentation?.content,
        name = case.name.name.toCamelCase(upperCamel = true),
        type = case.type?.let { typeMapper.map(it) },
      )
    },
  )

  fun mapEnum(typeMapper: InterfaceTypeMapper, value: Enum) = EnumKt(
    documentation = value.documentation?.content,
    type = typeMapper.className.nestedClass(value.name.name.toCamelCase(upperCamel = true)),
    cases = value.cases.map {
      check(it.type == null)
      EnumKt.Case(
        documentation = it.documentation?.content,
        name = it.name.name.toCamelCase(upperCamel = true),
      )
    },
  )

  fun mapFlags(typeMapper: InterfaceTypeMapper, value: Flags) = FlagsKt(
    documentation = value.documentation?.content,
    type = typeMapper.className.nestedClass(value.name.name.toCamelCase(upperCamel = true)),
    flags = value.flags.map { flag ->
      FlagsKt.Flag(
        documentation = flag.documentation?.content,
        name = flag.name.name.toCamelCase(upperCamel = false),
      )
    },
  )

  fun mapFunction(typeMapper: PackageTypeMapper, value: Function) = FunctionKt(
    documentation = value.documentation?.content,
    name = value.name.name.toCamelCase(upperCamel = false),
    parameters = value.parameters.map { parameter ->
      FunctionKt.Parameter(
        documentation = null, // TODO.
        name = parameter.name.name.toCamelCase(upperCamel = false),
        type = typeMapper.map(parameter.type),
      )
    },
    returnType = value.returnType?.let { typeMapper.map(it) },
  )

  fun mapWorld(typeMapper: PackageTypeMapper, value: World): WorldKt {
    val interfaceTypeMapper = typeMapper.refine(interfaceName = value.name)
    return WorldKt(
      documentation = value.documentation?.content,
      type = interfaceTypeMapper.className,
      imports = value.declarations.filterIsInstance<Import>().mapNotNull {
        val path = it.value as? ExternalUsePath ?: return@mapNotNull null
        WorldKt.Import(
          documentation = it.documentation?.content,
          name = (path.plainName ?: path.path.name).name.toCamelCase(upperCamel = false),
          type = interfaceTypeMapper.refine(path.path).className,
        )
      },
      exports = value.declarations.filterIsInstance<Export>().mapNotNull {
        val path = it.value as? ExternalUsePath ?: return@mapNotNull null
        WorldKt.Export(
          documentation = it.documentation?.content,
          name = (path.plainName ?: path.path.name).name.toCamelCase(upperCamel = false),
          type = interfaceTypeMapper.refine(path.path).className,
        )
      },
    )
  }
}
