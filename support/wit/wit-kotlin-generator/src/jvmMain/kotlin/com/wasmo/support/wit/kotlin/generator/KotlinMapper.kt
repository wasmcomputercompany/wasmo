package com.wasmo.support.wit.kotlin.generator

import com.wasmo.support.wit.Case
import com.wasmo.support.wit.Declaration
import com.wasmo.support.wit.Enum
import com.wasmo.support.wit.ExternalUsePath
import com.wasmo.support.wit.Field
import com.wasmo.support.wit.Flag
import com.wasmo.support.wit.Flags
import com.wasmo.support.wit.Function
import com.wasmo.support.wit.Include
import com.wasmo.support.wit.Interface
import com.wasmo.support.wit.Package
import com.wasmo.support.wit.Record
import com.wasmo.support.wit.Resource
import com.wasmo.support.wit.SymbolIndex
import com.wasmo.support.wit.TopLevelUse
import com.wasmo.support.wit.TypeAlias
import com.wasmo.support.wit.Use
import com.wasmo.support.wit.Variant
import com.wasmo.support.wit.WitPackage
import com.wasmo.support.wit.World
import com.wasmo.support.wit.WorldFlattener

/**
 * Directly converts WIT model types ([World], [Function], etc.) to a Kotlin equivalents ([WorldKt],
 * [FunctionKt], etc.).
 *
 * This performs the following transformations:
 *
 *  * Case mapping of names: `monotonic-clock` to `MonotonicClock`, `utc-offset` to `utcOffset`.
 *  * Type mapping: `s32` to `Int`, `optional<string>` to `String?`.
 *  * Flattening worlds by applying `include` declarations.
 */
class KotlinMapper(
  witPackages: List<WitPackage>,
  private val kotlinPackagePrefix: String = "wit",
) {
  private val index = SymbolIndex(witPackages)

  fun mapPackage(witPackage: WitPackage): WitPackageKt {
    val typeMapper = TypeMapper(
      index = index,
      kotlinPackagePrefix = kotlinPackagePrefix,
      packageName = witPackage.packageName,
    )

    return WitPackageKt(
      packageName = witPackage.packageName.toKotlin(kotlinPackagePrefix),
      declarations = witPackage.files.values.flatMap { witFile ->
        witFile.declarations.mapNotNull {
          mapDeclaration(
            typeMapper = typeMapper,
            value = it,
          )
        }
      },
    )
  }

  private fun mapDeclaration(typeMapper: TypeMapper, value: Declaration): DeclarationKt? {
    return when (value) {
      is Case -> error("unexpected call")
      is Enum -> mapEnum(typeMapper as InterfaceTypeMapper, value)
      is ExternalUsePath -> null
      is Field -> error("unexpected call")
      is Flag -> error("unexpected call")
      is Flags -> mapFlags(typeMapper as InterfaceTypeMapper, value)
      is Function -> mapFunction(typeMapper, value)
      is Include -> null
      is Include.Item -> error("unexpected call")
      is Interface -> mapInterface(typeMapper, value)
      is Package -> null
      is Record -> mapRecord(typeMapper as InterfaceTypeMapper, value)
      is Resource -> mapResource(typeMapper as InterfaceTypeMapper, value)
      is TopLevelUse -> null
      is TypeAlias -> mapTypeAlias(typeMapper as InterfaceTypeMapper, value)
      is Use -> null
      is Use.Item -> error("unexpected call")
      is Variant -> mapVariant(typeMapper as InterfaceTypeMapper, value)
      is World -> mapWorld(typeMapper, value)
    }
  }

  fun mapInterface(typeMapper: TypeMapper, value: Interface): InterfaceKt {
    val interfaceTypeMapper = typeMapper.refine(interfaceName = value.name)
    return InterfaceKt(
      documentation = value.documentation?.content,
      type = interfaceTypeMapper.className,
      instanceName = value.name.name.toCamelCase(upperCamel = false),
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

  fun mapTypeAlias(typeMapper: InterfaceTypeMapper, value: TypeAlias) = TypeAliasKt(
    documentation = value.documentation?.content,
    type = typeMapper.className.nestedClass(value.name.name.toCamelCase(upperCamel = true)),
    target = typeMapper.map(value.target),
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

  fun mapFunction(typeMapper: TypeMapper, value: Function) = FunctionKt(
    documentation = value.documentation?.content,
    name = value.name.name.toCamelCase(upperCamel = false),
    parameters = value.parameters.map { parameter ->
      FunctionKt.Parameter(
        documentation = parameter.documentation?.content,
        name = parameter.name.name.toCamelCase(upperCamel = false),
        type = typeMapper.map(parameter.type),
      )
    },
    returnType = value.returnType?.let { typeMapper.map(it) },
  )

  fun mapWorld(typeMapper: TypeMapper, value: World): WorldKt {
    val flattener = WorldFlattener(index)
    val flattened = flattener.flatten(
      world = value,
      inPackageName = typeMapper.packageName,
    )
    val interfaceTypeMapper = typeMapper.refine(interfaceName = flattened.name)
    return WorldKt(
      documentation = flattened.documentation?.content,
      type = interfaceTypeMapper.className,
      declarations = flattened.declarations.mapNotNull {
        mapDeclaration(interfaceTypeMapper, it)
      },
      hostApis = flattened.imports.map {
        mapWorldApi(interfaceTypeMapper, it)
      },
      guestApis = flattened.exports.map {
        mapWorldApi(interfaceTypeMapper, it)
      },
    )
  }

  private fun mapWorldApi(
    typeMapper: InterfaceTypeMapper,
    value: World.Api,
  ): WorldKt.Api {
    return when (value) {
      is ExternalUsePath -> ExternalUsePathKt(
        documentation = value.documentation?.content,
        name = (value.plainName ?: value.path.name).name.toCamelCase(upperCamel = false),
        type = typeMapper.refine(value.path).className,
      )

      is Function -> mapFunction(typeMapper, value)
      is Interface -> mapInterface(typeMapper, value)
    }
  }
}
