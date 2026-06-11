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
 * Directly converts WIT model types ([IoWorld], [IoFunction], etc.) to a Kotlin equivalents
 * ([WorldKt], [FunctionKt], etc.).
 */
class KotlinMapper(
  private val kotlinPackagePrefix: String = "wit",
) {
  private val typeMapper = TypeMapper(kotlinPackagePrefix)

  fun map(witPackage: IrWitPackage): WitPackageKt {
    val kotlinName = witPackage.packageName.toKotlin(kotlinPackagePrefix)
    context(Context(kotlinName)) {
      return WitPackageKt(
        packageName = kotlinName.name,
        declarations = witPackage.items.mapNotNull { declaration ->
          declaration.packageItemToKt()
        },
      )
    }
  }

  context(context: Context)
  internal fun IrWitPackage.Item.packageItemToKt(): DeclarationKt? {
    return when (this) {
      is IrInterface -> interfaceToKt()
      is IrWorld -> worldToKt()
    }
  }

  context(context: Context)
  internal fun IrInterface.Item.interfaceItemToKt(): DeclarationKt? {
    return when (this) {
      is IrEnum -> enumToKt()
      is IrFlags -> flagsToKt()
      is IrFunction -> functionToKt()
      is IrRecord -> recordToKt()
      is IrResource -> resourceToKt()
      is IrTypeAlias -> typeAliasToKt()
      is IrVariant -> variantToKt()
    }
  }

  context(context: Context)
  internal fun IrWorld.Item.worldItemToKt(): DeclarationKt? {
    return when (this) {
      is IrEnum -> enumToKt()
      is IrFlags -> flagsToKt()
      is IrRecord -> recordToKt()
      is IrResource -> resourceToKt()
      is IrTypeAlias -> typeAliasToKt()
      is IrVariant -> variantToKt()
    }
  }

  context(context: Context)
  internal fun IrInterface.interfaceToKt(): InterfaceKt {
    val kotlinName = context.kotlinName + name
    context(Context(kotlinName)) {
      return InterfaceKt(
        documentation = documentation?.content,
        type = kotlinName.name,
        instanceName = name.name.toCamelCase(upperCamel = false),
        declarations = items.mapNotNull {
          it.interfaceItemToKt()
        },
      )
    }
  }

  context(context: Context)
  internal fun IrRecord.recordToKt() = RecordKt(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    fields = fields.map { field ->
      RecordKt.Field(
        documentation = field.documentation?.content,
        name = field.name.name.toCamelCase(upperCamel = false),
        type = typeMapper.map(field.type),
      )
    },
  )

  context(context: Context)
  internal fun IrResource.resourceToKt() = ResourceKt(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    functions = functions.map { it.functionToKt() },
  )

  context(context: Context)
  internal fun IrTypeAlias.typeAliasToKt() = TypeAliasKt(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    target = typeMapper.map(target),
  )

  context(context: Context)
  internal fun IrVariant.variantToKt() = VariantKt(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    cases = cases.map { case ->
      VariantKt.Case(
        documentation = case.documentation?.content,
        name = case.name.name.toCamelCase(upperCamel = true),
        type = case.type?.let { typeMapper.map(it) },
      )
    },
  )

  context(context: Context)
  internal fun IrEnum.enumToKt() = EnumKt(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    cases = cases.map {
      check(it.type == null)
      EnumKt.Case(
        documentation = it.documentation?.content,
        name = it.name.name.toCamelCase(upperCamel = true),
      )
    },
  )

  context(context: Context)
  internal fun IrFlags.flagsToKt() = FlagsKt(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    flags = flags.map { flag ->
      FlagsKt.Flag(
        documentation = flag.documentation?.content,
        name = flag.name.name.toCamelCase(upperCamel = false),
      )
    },
  )

  context(context: Context)
  internal fun IrFunction.functionToKt() = FunctionKt(
    documentation = documentation?.content,
    name = name.name.toCamelCase(upperCamel = false),
    parameters = parameters.map { parameter ->
      FunctionKt.Parameter(
        documentation = parameter.documentation?.content,
        name = parameter.name.name.toCamelCase(upperCamel = false),
        type = typeMapper.map(parameter.type),
      )
    },
    returnType = returnType?.let { typeMapper.map(it) },
  )

  context(context: Context)
  internal fun IrWorld.worldToKt(): WorldKt {
    val kotlinName = context.kotlinName + name
    val hostName = kotlinName + Identifier("Host")
    val guestName = kotlinName + Identifier("Guest")
    return WorldKt(
      documentation = documentation?.content,
      type = kotlinName.name,
      declarations = context(Context(kotlinName)) {
        items.mapNotNull { it.worldItemToKt() }
      },
      host = context(Context(hostName)) {
        WorldKt.Host(
          type = hostName.name,
          apis = imports.map { it.worldApiToKt() },
        )
      },
      guest = context(Context(guestName)) {
        WorldKt.Guest(
          type = guestName.name,
          apis = exports.map { it.worldApiToKt() },
        )
      },
    )
  }

  context(context: Context)
  private fun IrWorld.Api.worldApiToKt(): WorldKt.Api {
    return when (this) {
      is IrExternalApi -> {
        ExternalUsePathKt(
          documentation = documentation?.content,
          name = (plainName ?: path.name).name.toCamelCase(upperCamel = false),
          type = typeMapper.map(path),
        )
      }

      is IrFunction -> functionToKt()
      is IrInterface -> interfaceToKt()
    }
  }

  internal class Context(
    val kotlinName: KotlinName,
  )
}
