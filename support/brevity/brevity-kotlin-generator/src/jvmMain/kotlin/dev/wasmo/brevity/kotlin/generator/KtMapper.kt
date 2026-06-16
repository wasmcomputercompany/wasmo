package dev.wasmo.brevity.kotlin.generator

import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.NameAllocator
import dev.wasmo.brevity.Identifier
import dev.wasmo.brevity.io.IoFunction
import dev.wasmo.brevity.io.IoWorld
import dev.wasmo.brevity.ir.IrEnum
import dev.wasmo.brevity.ir.IrExternalApi
import dev.wasmo.brevity.ir.IrFlags
import dev.wasmo.brevity.ir.IrFunction
import dev.wasmo.brevity.ir.IrInterface
import dev.wasmo.brevity.ir.IrRecord
import dev.wasmo.brevity.ir.IrResource
import dev.wasmo.brevity.ir.IrTypeAlias
import dev.wasmo.brevity.ir.IrVariant
import dev.wasmo.brevity.ir.IrWitPackage
import dev.wasmo.brevity.ir.IrWorld

/**
 * Directly converts WIT model types ([IoWorld], [IoFunction], etc.) to a Kotlin equivalents
 * ([KtWorld], [KtFunction], etc.).
 */
class KtMapper(
  private val kotlinPackagePrefix: String = "wit",
  private val worldFilter: (IrWorld) -> Boolean = { true },
  private val onlyLongs: Boolean = false,
) {
  private val typeMapper = TypeMapper(kotlinPackagePrefix)

  fun map(witPackage: IrWitPackage): KtWitPackage {
    val kotlinName = witPackage.packageName.toKotlin(kotlinPackagePrefix)
    context(Context(kotlinName, NameAllocator())) {
      return KtWitPackage(
        packageName = kotlinName.name,
        items = witPackage.items.mapNotNull { declaration ->
          declaration.packageItemToKt()
        },
      )
    }
  }

  context(context: Context)
  internal fun IrWitPackage.Item.packageItemToKt(): KtWitPackage.Item? {
    return when (this) {
      is IrInterface -> interfaceToKt()
      is IrWorld -> worldToKt()
    }
  }

  context(context: Context)
  internal fun IrInterface.Item.interfaceItemToKt(): KtInterface.Item? {
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
  internal fun IrWorld.Item.worldItemToKt(): KtWorld.Item? {
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
  internal fun IrInterface.interfaceToKt(): KtInterface {
    val kotlinName = context.kotlinName + name
    context(context.copy(kotlinName = kotlinName)) {
      return KtInterface(
        documentation = documentation?.content,
        type = kotlinName.name,
        instanceName = name.name.toCamelCase(upperCamel = false),
        items = items.mapNotNull {
          it.interfaceItemToKt()
        },
      )
    }
  }

  context(context: Context)
  internal fun IrRecord.recordToKt() = KtRecord(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    fields = fields.map { field ->
      KtRecord.Field(
        documentation = field.documentation?.content,
        name = field.name.name.toCamelCase(upperCamel = false),
        type = typeMapper.map(field.type),
      )
    },
  )

  context(context: Context)
  internal fun IrResource.resourceToKt() = KtResource(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    functions = functions.map { it.functionToKt() },
  )

  context(context: Context)
  internal fun IrTypeAlias.typeAliasToKt() = KtTypeAlias(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    target = typeMapper.map(target),
  )

  context(context: Context)
  internal fun IrVariant.variantToKt() = KtVariant(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    cases = cases.map { case ->
      KtVariant.Case(
        documentation = case.documentation?.content,
        name = case.name.name.toCamelCase(upperCamel = true),
        type = case.type?.let { typeMapper.map(it) },
      )
    },
  )

  context(context: Context)
  internal fun IrEnum.enumToKt() = KtEnum(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    cases = cases.map {
      check(it.type == null)
      KtEnum.Case(
        documentation = it.documentation?.content,
        name = it.name.name.toCamelCase(upperCamel = true),
      )
    },
  )

  context(context: Context)
  internal fun IrFlags.flagsToKt() = KtFlags(
    documentation = documentation?.content,
    type = (context.kotlinName + name).name,
    flags = flags.map { flag ->
      KtFlags.Flag(
        documentation = flag.documentation?.content,
        name = flag.name.name.toCamelCase(upperCamel = false),
      )
    },
  )

  context(context: Context)
  internal fun IrFunction.functionToKt() = KtFunction(
    documentation = documentation?.content,
    ktName = functionName.name.name.toCamelCase(upperCamel = false),
    name = functionName,
    parameters = parameters.map { parameter ->
      KtFunction.Parameter(
        documentation = parameter.documentation?.content,
        name = parameter.name.name.toCamelCase(upperCamel = false),
        type = when {
          onlyLongs -> LONG
          else -> typeMapper.map(parameter.type)
        },
      )
    },
    returnType = returnType?.let {
      when {
        onlyLongs -> LONG
        else -> typeMapper.map(it)
      }
    },
  )

  context(context: Context)
  internal fun IrWorld.worldToKt(): KtWorld? {
    if (!worldFilter.invoke(this)) return null

    val kotlinName = context.kotlinName + name
    val hostName = kotlinName + Identifier("Host")
    val guestName = kotlinName + Identifier("Guest")
    return KtWorld(
      documentation = documentation?.content,
      type = kotlinName.name,
      items = context(context.copy(kotlinName = kotlinName)) {
        items.mapNotNull { it.worldItemToKt() }
      },
      host = context(context.copy(kotlinName = hostName)) {
        KtWorld.Host(
          name = context.nameAllocator.newName("host"),
          type = hostName.name,
          apis = imports.map { it.worldApiToKt() },
        )
      },
      guest = context(context.copy(kotlinName = guestName)) {
        KtWorld.Guest(
          name = context.nameAllocator.newName("guest"),
          type = guestName.name,
          apis = exports.map { it.worldApiToKt() },
        )
      },
    )
  }

  context(context: Context)
  private fun IrWorld.Api.worldApiToKt(): KtWorld.Api {
    return when (this) {
      is IrExternalApi -> {
        KtExternalApi(
          documentation = documentation?.content,
          name = (plainName ?: path.name).name.toCamelCase(upperCamel = false),
          type = typeMapper.map(path),
          functions = functions.map { it.functionToKt() },
        )
      }

      is IrFunction -> functionToKt()
      is IrInterface -> interfaceToKt()
    }
  }

  internal data class Context(
    val kotlinName: KotlinName,
    val nameAllocator: NameAllocator,
  )
}
