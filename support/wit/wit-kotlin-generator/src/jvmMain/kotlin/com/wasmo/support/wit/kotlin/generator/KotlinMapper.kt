package com.wasmo.support.wit.kotlin.generator

import com.wasmo.support.wit.Case
import com.wasmo.support.wit.Declaration
import com.wasmo.support.wit.Enum
import com.wasmo.support.wit.ExternalUsePath
import com.wasmo.support.wit.Field
import com.wasmo.support.wit.Flag
import com.wasmo.support.wit.Flags
import com.wasmo.support.wit.Function
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.Include
import com.wasmo.support.wit.Interface
import com.wasmo.support.wit.Package
import com.wasmo.support.wit.Record
import com.wasmo.support.wit.Resource
import com.wasmo.support.wit.Scope
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
    val nameMapper = witPackage.packageName.toNameMapper(kotlinPackagePrefix)
    return WitPackageKt(
      packageName = nameMapper.packageName,
      declarations = witPackage.files.values.flatMap { witFile ->
        witFile.declarations.mapNotNull { declaration ->
          mapDeclaration(
            nameMapper = nameMapper,
            typeMapper = TypeMapper(
              index = index,
              kotlinPackagePrefix = kotlinPackagePrefix,
              scope = Scope(packageName = witPackage.packageName),
            ),
            value = declaration,
          )
        }
      },
    )
  }

  private fun mapDeclaration(
    nameMapper: NameMapper,
    typeMapper: TypeMapper,
    value: Declaration,
  ): DeclarationKt? {
    return when (value) {
      is Case -> error("unexpected call")
      is Enum -> mapEnum(nameMapper, typeMapper, value)
      is ExternalUsePath -> null
      is Field -> error("unexpected call")
      is Flag -> error("unexpected call")
      is Flags -> mapFlags(nameMapper, typeMapper, value)
      is Function -> mapFunction(nameMapper, typeMapper, value)
      is Include -> null
      is Include.Item -> error("unexpected call")
      is Interface -> mapInterface(nameMapper, typeMapper, value)
      is Package -> null
      is Record -> mapRecord(nameMapper, typeMapper, value)
      is Resource -> mapResource(nameMapper, typeMapper, value)
      is TopLevelUse -> null
      is TypeAlias -> mapTypeAlias(nameMapper, typeMapper, value)
      is Use -> null
      is Use.Item -> error("unexpected call")
      is Variant -> mapVariant(nameMapper, typeMapper, value)
      is World -> mapWorld(nameMapper, typeMapper, value)
    }
  }

  fun mapInterface(
    nameMapper: NameMapper,
    typeMapper: TypeMapper,
    value: Interface,
  ): InterfaceKt {
    val nameMapper = nameMapper + value.name
    val typeMapper = typeMapper.withScope(interfaceName = value.name)
    return InterfaceKt(
      documentation = value.documentation?.content,
      type = nameMapper.className,
      instanceName = value.name.name.toCamelCase(upperCamel = false),
      declarations = value.declarations.mapNotNull {
        mapDeclaration(nameMapper, typeMapper, it)
      },
    )
  }

  fun mapRecord(
    nameMapper: NameMapper,
    typeMapper: TypeMapper,
    value: Record,
  ) = RecordKt(
    documentation = value.documentation?.content,
    type = (nameMapper + value.name).className,
    fields = value.fields.map { field ->
      RecordKt.Field(
        documentation = field.documentation?.content,
        name = field.name.name.toCamelCase(upperCamel = false),
        type = typeMapper.map(field.type),
      )
    },
  )

  fun mapResource(
    nameMapper: NameMapper,
    typeMapper: TypeMapper,
    value: Resource,
  ) = ResourceKt(
    documentation = value.documentation?.content,
    type = (nameMapper + value.name).className,
    functions = value.functions.map { mapFunction(nameMapper, typeMapper, it) },
  )

  fun mapTypeAlias(
    nameMapper: NameMapper,
    typeMapper: TypeMapper,
    value: TypeAlias,
  ) = TypeAliasKt(
    documentation = value.documentation?.content,
    type = (nameMapper + value.name).className,
    target = typeMapper.map(value.target),
  )

  fun mapVariant(
    nameMapper: NameMapper,
    typeMapper: TypeMapper,
    value: Variant,
  ) = VariantKt(
    documentation = value.documentation?.content,
    type = (nameMapper + value.name).className,
    cases = value.cases.map { case ->
      VariantKt.Case(
        documentation = case.documentation?.content,
        name = case.name.name.toCamelCase(upperCamel = true),
        type = case.type?.let { typeMapper.map(it) },
      )
    },
  )

  fun mapEnum(
    nameMapper: NameMapper,
    typeMapper: TypeMapper,
    value: Enum,
  ) = EnumKt(
    documentation = value.documentation?.content,
    type = (nameMapper + value.name).className,
    cases = value.cases.map {
      check(it.type == null)
      EnumKt.Case(
        documentation = it.documentation?.content,
        name = it.name.name.toCamelCase(upperCamel = true),
      )
    },
  )

  fun mapFlags(
    nameMapper: NameMapper,
    typeMapper: TypeMapper,
    value: Flags,
  ) = FlagsKt(
    documentation = value.documentation?.content,
    type = (nameMapper + value.name).className,
    flags = value.flags.map { flag ->
      FlagsKt.Flag(
        documentation = flag.documentation?.content,
        name = flag.name.name.toCamelCase(upperCamel = false),
      )
    },
  )

  fun mapFunction(
    nameMapper: NameMapper,
    typeMapper: TypeMapper,
    value: Function,
  ) = FunctionKt(
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

  fun mapWorld(
    nameMapper: NameMapper,
    typeMapper: TypeMapper,
    value: World,
  ): WorldKt {
    val flattener = WorldFlattener(index)
    val flattened = flattener.flatten(
      world = value,
      inPackageName = typeMapper.scope.packageName,
    )
    val typeMapper = typeMapper.withScope(interfaceName = flattened.name)
    val nameMapper = nameMapper + value.name
    val hostNameMapper = nameMapper + Identifier("Host")
    val guestNameMapper = nameMapper + Identifier("Guest")
    return WorldKt(
      documentation = flattened.documentation?.content,
      type = nameMapper.className,
      declarations = flattened.declarations.mapNotNull {
        mapDeclaration(nameMapper, typeMapper, it)
      },
      host = WorldKt.Host(
        type = hostNameMapper.className,
        apis = flattened.imports.map { mapWorldApi(hostNameMapper, typeMapper, it) },
      ),
      guest = WorldKt.Guest(
        type = guestNameMapper.className,
        apis = flattened.exports.map { mapWorldApi(guestNameMapper, typeMapper, it) },
      ),
    )
  }

  private fun mapWorldApi(
    nameMapper: NameMapper,
    typeMapper: TypeMapper,
    value: World.Api,
  ): WorldKt.Api {
    return when (value) {
      is ExternalUsePath -> {
        val packageName = typeMapper.withScope(usePath = value.path).scope.packageName
        ExternalUsePathKt(
          documentation = value.documentation?.content,
          name = (value.plainName ?: value.path.name).name.toCamelCase(upperCamel = false),
          type = packageName.toNameMapper(kotlinPackagePrefix).plus(value.path.name).className,
        )
      }

      is Function -> mapFunction(nameMapper, typeMapper, value)
      is Interface -> mapInterface(nameMapper, typeMapper, value)
    }
  }
}
