package com.wasmo.support.wit.kotlin.generator

import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.io.IoFunction
import com.wasmo.support.wit.io.IoWorld
import com.wasmo.support.wit.ir.IrEnum
import com.wasmo.support.wit.ir.IrExternalApi
import com.wasmo.support.wit.ir.IrFlags
import com.wasmo.support.wit.ir.IrFunction
import com.wasmo.support.wit.ir.IrInterface
import com.wasmo.support.wit.ir.IrRecord
import com.wasmo.support.wit.ir.IrResource
import com.wasmo.support.wit.ir.IrTypeAlias
import com.wasmo.support.wit.ir.IrVariant
import com.wasmo.support.wit.ir.IrWitPackage
import com.wasmo.support.wit.ir.IrWorld

/**
 * Directly converts WIT model types ([IoWorld], [IoFunction], etc.) to a Kotlin equivalents ([WorldKt],
 * [FunctionKt], etc.).
 *
 * This performs the following transformations:
 *
 *  * Case mapping of names: `monotonic-clock` to `MonotonicClock`, `utc-offset` to `utcOffset`.
 *  * Type mapping: `s32` to `Int`, `optional<string>` to `String?`.
 *  * Flattening worlds by applying `include` declarations.
 */
class KotlinMapper(
  private val kotlinPackagePrefix: String = "wit",
) {
  private val typeMapper = TypeMapper(kotlinPackagePrefix)

  fun mapPackage(witPackage: IrWitPackage): WitPackageKt {
    val nameMapper = witPackage.packageName.toNameMapper(kotlinPackagePrefix)
    return WitPackageKt(
      packageName = nameMapper.packageName,
      declarations = witPackage.items.mapNotNull { declaration ->
        mapPackageItem(
          nameMapper = nameMapper,
          value = declaration,
        )
      },
    )
  }

  private fun mapPackageItem(
    nameMapper: NameMapper,
    value: IrWitPackage.Item,
  ): DeclarationKt? {
    return when (value) {
      is IrInterface -> mapInterface(nameMapper, value)
      is IrWorld -> mapWorld(nameMapper, value)
    }
  }

  private fun mapInterfaceItem(
    nameMapper: NameMapper,
    value: IrInterface.Item,
  ): DeclarationKt? {
    return when (value) {
      is IrEnum -> mapEnum(nameMapper, value)
      is IrFlags -> mapFlags(nameMapper, value)
      is IrFunction -> mapFunction(nameMapper, value)
      is IrRecord -> mapRecord(nameMapper, value)
      is IrResource -> mapResource(nameMapper, value)
      is IrTypeAlias -> mapTypeAlias(nameMapper, value)
      is IrVariant -> mapVariant(nameMapper, value)
    }
  }

  private fun mapWorldItem(
    nameMapper: NameMapper,
    value: IrWorld.Item,
  ): DeclarationKt? {
    return when (value) {
      is IrEnum -> mapEnum(nameMapper, value)
      is IrFlags -> mapFlags(nameMapper, value)
      is IrRecord -> mapRecord(nameMapper, value)
      is IrResource -> mapResource(nameMapper, value)
      is IrTypeAlias -> mapTypeAlias(nameMapper, value)
      is IrVariant -> mapVariant(nameMapper, value)
    }
  }

  fun mapInterface(
    nameMapper: NameMapper,
    value: IrInterface,
  ): InterfaceKt {
    val nameMapper = nameMapper + value.name
    return InterfaceKt(
      documentation = value.documentation?.content,
      type = nameMapper.className,
      instanceName = value.name.name.toCamelCase(upperCamel = false),
      declarations = value.items.mapNotNull {
        mapInterfaceItem(nameMapper, it)
      },
    )
  }

  fun mapRecord(
    nameMapper: NameMapper,
    value: IrRecord,
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
    value: IrResource,
  ) = ResourceKt(
    documentation = value.documentation?.content,
    type = (nameMapper + value.name).className,
    functions = value.functions.map { mapFunction(nameMapper, it) },
  )

  fun mapTypeAlias(
    nameMapper: NameMapper,
    value: IrTypeAlias,
  ) = TypeAliasKt(
    documentation = value.documentation?.content,
    type = (nameMapper + value.name).className,
    target = typeMapper.map(value.target),
  )

  fun mapVariant(
    nameMapper: NameMapper,
    value: IrVariant,
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
    value: IrEnum,
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
    value: IrFlags,
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
    value: IrFunction,
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
    value: IrWorld,
  ): WorldKt {
    val nameMapper = nameMapper + value.name
    val hostNameMapper = nameMapper + Identifier("Host")
    val guestNameMapper = nameMapper + Identifier("Guest")
    return WorldKt(
      documentation = value.documentation?.content,
      type = nameMapper.className,
      declarations = value.items.mapNotNull {
        mapWorldItem(nameMapper, it)
      },
      host = WorldKt.Host(
        type = hostNameMapper.className,
        apis = value.imports.map { mapWorldApi(hostNameMapper, it) },
      ),
      guest = WorldKt.Guest(
        type = guestNameMapper.className,
        apis = value.exports.map { mapWorldApi(guestNameMapper, it) },
      ),
    )
  }

  private fun mapWorldApi(
    nameMapper: NameMapper,
    value: IrWorld.Api,
  ): WorldKt.Api {
    return when (value) {
      is IrExternalApi -> {
        ExternalUsePathKt(
          documentation = value.documentation?.content,
          name = (value.plainName ?: value.path.name).name.toCamelCase(upperCamel = false),
          type = typeMapper.map(value.path),
        )
      }

      is IrFunction -> mapFunction(nameMapper, value)
      is IrInterface -> mapInterface(nameMapper, value)
    }
  }
}
