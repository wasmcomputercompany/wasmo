package dev.wasmo.brevity.ir

import dev.wasmo.brevity.Annotation
import dev.wasmo.brevity.Documentation
import dev.wasmo.brevity.FunctionName
import dev.wasmo.brevity.Identifier
import dev.wasmo.brevity.PackageName
import dev.wasmo.brevity.io.IoCase
import dev.wasmo.brevity.io.IoEnum
import dev.wasmo.brevity.io.IoExternalApi
import dev.wasmo.brevity.io.IoField
import dev.wasmo.brevity.io.IoFlag
import dev.wasmo.brevity.io.IoFlags
import dev.wasmo.brevity.io.IoFunction
import dev.wasmo.brevity.io.IoInclude
import dev.wasmo.brevity.io.IoInlinePackage
import dev.wasmo.brevity.io.IoInterface
import dev.wasmo.brevity.io.IoParameter
import dev.wasmo.brevity.io.IoRecord
import dev.wasmo.brevity.io.IoResource
import dev.wasmo.brevity.io.IoTopLevelUse
import dev.wasmo.brevity.io.IoTypeAlias
import dev.wasmo.brevity.io.IoTypeDeclaration
import dev.wasmo.brevity.io.IoTypeName
import dev.wasmo.brevity.io.IoUse
import dev.wasmo.brevity.io.IoVariant
import dev.wasmo.brevity.io.IoWitPackage
import dev.wasmo.brevity.io.IoWorld
import dev.wasmo.brevity.io.Keywords
import dev.wasmo.brevity.io.UsePath

class IrMapper(
  private val packages: List<IoWitPackage>,
) {
  private val packageNameToPackage = packages.associateBy { it.packageName }
  private val irPackages = mutableMapOf<PackageName, PackageBuilder>()

  internal class PackageBuilder {
    val documentation = mutableListOf<Documentation>()
    val items = mutableListOf<IrWitPackage.Item>()
  }

  fun map(): List<IrWitPackage> {
    check(irPackages.isEmpty())

    for (ioPackage in packages) {
      addPackage(ioPackage)
    }

    return irPackages.map { (name, builder) ->
      IrWitPackage(
        packageDocumentation = when {
          builder.documentation.isEmpty() -> null
          else -> Documentation(builder.documentation.joinToString("\n") { it.content })
        },
        packageName = name,
        items = builder.items,
      )
    }
  }

  private fun addPackage(ioPackage: IoWitPackage) {
    val builder = irPackages.getOrPut(ioPackage.packageName) { PackageBuilder() }

    for ((_, ioFile) in ioPackage.files) {
      for (item in ioFile.items) {
        when (item) {
          is IoInterface -> builder.items += item.interfaceToIr(ioPackage.packageName)
          is IoWorld -> builder.items += item.worldToIr(ioPackage.packageName)
          is IoTopLevelUse -> {}
          is IoInlinePackage -> {}
        }
      }
    }
  }

  private fun IoInterface.interfaceToIr(packageName: PackageName): IrInterface {
    context(Context(packageName, name)) {
      return IrInterface(
        documentation = documentation,
        gate = gate,
        offset = offset,
        name = name,
        items = items.mapNotNull { item ->
          item.interfaceItemToIrOrNull()
        },
      )
    }
  }

  context(context: Context)
  private fun IoInterface.Item.interfaceItemToIrOrNull(): IrInterface.Item? {
    return when (this) {
      is IoFunction -> functionToIr()
      is IoEnum -> enumToIr()
      is IoFlags -> flagsToIr()
      is IoRecord -> recordToIr()
      is IoResource -> resourceToIr()
      is IoTypeAlias -> typeAliasToIr()
      is IoVariant -> variantToIr()
      is IoUse -> null
    }
  }

  context(context: Context)
  private fun IoCase.caseToIr() = IrCase(
    documentation = documentation,
    gate = gate,
    offset = offset,
    name = name,
    type = type?.typeNameToIr(),
  )

  context(context: Context)
  private fun IoField.fieldToIr() = IrField(
    documentation = documentation,
    gate = gate,
    offset = offset,
    name = name,
    type = type.typeNameToIr(),
  )

  context(context: Context)
  private fun IoFlag.flagToIr() = IrFlag(
    documentation = documentation,
    gate = gate,
    offset = offset,
    name = name,
  )

  context(context: Context)
  private fun IoFunction.functionToIr(resourceName: Identifier? = null) = IrFunction(
    documentation = documentation,
    gate = gate,
    offset = offset,
    async = async,
    parameters = parameters.map { it.parameterToIr() },
    returnType = returnType?.typeNameToIr(),
    functionName = when {
      constructor && resourceName != null && name == Keywords.constructor -> FunctionName(
        packageName = context.packageName,
        parentName = context.parentName,
        name = resourceName,
        annotation = Annotation.Constructor,
      )

      static && resourceName != null -> FunctionName(
        packageName = context.packageName,
        parentName = context.parentName,
        resourceName = resourceName,
        name = name,
        annotation = Annotation.Static,
      )

      resourceName != null -> FunctionName(
        packageName = context.packageName,
        parentName = context.parentName,
        resourceName = resourceName,
        name = name,
        annotation = Annotation.Method,
      )

      else -> FunctionName(
        packageName = context.packageName,
        parentName = context.parentName,
        name = name,
      )
    },
  )

  context(context: Context)
  private fun IoParameter.parameterToIr() = IrParameter(
    documentation = documentation,
    offset = offset,
    name = name,
    type = type.typeNameToIr(),
  )

  context(context: Context)
  private fun IoEnum.enumToIr() = IrEnum(
    documentation = documentation,
    gate = gate,
    offset = offset,
    name = name,
    cases = cases.map { it.caseToIr() },
  )

  context(context: Context)
  private fun IoFlags.flagsToIr() = IrFlags(
    documentation = documentation,
    gate = gate,
    offset = offset,
    name = name,
    flags = flags.map { it.flagToIr() },
  )

  context(context: Context)
  private fun IoRecord.recordToIr() = IrRecord(
    documentation = documentation,
    gate = gate,
    offset = offset,
    name = name,
    fields = fields.map { it.fieldToIr() },
  )

  context(context: Context)
  private fun IoResource.resourceToIr() = IrResource(
    documentation = documentation,
    gate = gate,
    offset = offset,
    name = name,
    functions = functions.map {
      it.functionToIr(resourceName = name)
    },
  )

  context(context: Context)
  private fun IoTypeAlias.typeAliasToIr() = IrTypeAlias(
    documentation = documentation,
    gate = gate,
    offset = offset,
    name = name,
    target = target.typeNameToIr(),
  )

  context(context: Context)
  private fun IoVariant.variantToIr() = IrVariant(
    documentation = documentation,
    gate = gate,
    offset = offset,
    name = name,
    cases = cases.map { it.caseToIr() },
  )

  context(context: Context)
  private fun IoExternalApi.externalUsePathToIr(): IrExternalApi {
    val parentName = path.usePathToIr()
    return IrExternalApi(
      documentation = documentation,
      gate = gate,
      offset = offset,
      plainName = plainName,
      path = parentName,
      functions = context(Context(parentName.packageName, parentName.name)) {
        val interfaceItems = getInterfaceOrNull(parentName.usePath)?.items ?: listOf()
        interfaceItems.filterIsInstance<IoFunction>().map { it.functionToIr() }
      },
    )
  }

  context(context: Context)
  private fun UsePath.usePathToIr() = IrParentName(
    packageName = packageName ?: context.packageName,
    name = name,
  )

  context(context: Context)
  internal fun IoTypeName.typeNameToIr(): IrTypeName {
    return when (this) {
      IoTypeName.Bool -> IrTypeName.Bool
      IoTypeName.S8 -> IrTypeName.S8
      IoTypeName.S16 -> IrTypeName.S16
      IoTypeName.S32 -> IrTypeName.S32
      IoTypeName.S64 -> IrTypeName.S64
      IoTypeName.U8 -> IrTypeName.U8
      IoTypeName.U16 -> IrTypeName.U16
      IoTypeName.U32 -> IrTypeName.U32
      IoTypeName.U64 -> IrTypeName.U64
      IoTypeName.F32 -> IrTypeName.F32
      IoTypeName.F64 -> IrTypeName.F64
      IoTypeName.Char -> IrTypeName.Char
      IoTypeName.String -> IrTypeName.String
      is IoTypeName.Borrow -> IrTypeName.Borrow(type.typeNameToIr())
      is IoTypeName.Declared -> declaredTypeToIr()
      is IoTypeName.Future -> IrTypeName.Future(type?.typeNameToIr())
      is IoTypeName.List -> IrTypeName.List(type.typeNameToIr(), size)
      is IoTypeName.Map -> IrTypeName.Map(key.typeNameToIr(), value.typeNameToIr())
      is IoTypeName.Option -> IrTypeName.Option(type.typeNameToIr())
      is IoTypeName.Result -> IrTypeName.Result(ok?.typeNameToIr(), err?.typeNameToIr())
      is IoTypeName.Stream -> IrTypeName.Stream(type?.typeNameToIr())
      is IoTypeName.Tuple -> IrTypeName.Tuple(types.map { it.typeNameToIr() })
    }
  }

  context(context: Context)
  private fun IoTypeName.Declared.declaredTypeToIr(): IrTypeName.Declared {
    return declaredTypeToIrOrNull()
      ?: throw IllegalArgumentException(
        "unable to find $this in ${UsePath(context.packageName, context.parentName)}",
      )
  }

  context(context: Context)
  internal fun IoTypeName.Declared.declaredTypeToIrOrNull(): IrTypeName.Declared? {
    val witPackage = packageNameToPackage[context.packageName] ?: return null
    val declarations = witPackage.files.values
      .flatMap { it.items }
      .filterIsInstance<IoInterface>()
      .filter { it.name == context.parentName }
      .flatMap { it.items }

    for (declaration in declarations) {
      when (declaration) {
        is IoTypeDeclaration -> {
          // Direct match.
          if (declaration.name == name) {
            return IrTypeName.Declared(
              witPackage.packageName,
              context.parentName,
              declaration.name,
            )
          }
        }

        is IoUse -> {
          // Matched a 'use' statement that refers to another symbol.
          val itemMatch = declaration.items.firstOrNull { it.matches(this) }
          if (itemMatch != null) {
            val useContext = Context(
              packageName = declaration.path.packageName ?: context.packageName,
              parentName = declaration.path.name,
            )
            context(useContext) {
              return itemMatch.type.declaredTypeToIrOrNull()
            }
          }
        }

        else -> {}
      }
    }

    return null
  }

  /** Collect includes recursively. */
  private fun IoWorld.worldToIr(packageName: PackageName): IrWorld {
    val seed = IncludedWorld(
      packageName = packageName,
      world = this,
    )

    val set = LinkedHashSet<IncludedWorld>()
    seed.collectIncludesRecursively(set)

    return IrWorld(
      documentation = documentation,
      gate = gate,
      offset = offset,
      name = name,
      items = set.flatMap { included ->
        context(included.context) {
          included.world.items.mapNotNull { it.worldItemToIrOrNull() }
        }
      },
      imports = set.flatMap { included ->
        context(included.context) {
          included.world.imports.mapNotNull { it.worldApiToIr() }
        }
      },
      exports = set.flatMap { included ->
        context(included.context) {
          included.world.exports.mapNotNull { it.worldApiToIr() }
        }
      },
    )
  }

  context(context: Context)
  private fun IoWorld.Item.worldItemToIrOrNull(): IrWorld.Item? {
    return when (this) {
      is IoInclude -> null
      is IoEnum -> enumToIr()
      is IoFlags -> flagsToIr()
      is IoRecord -> recordToIr()
      is IoResource -> resourceToIr()
      is IoTypeAlias -> typeAliasToIr()
      is IoVariant -> variantToIr()
      is IoUse -> null
    }
  }

  /**
   * Returns null if the external API doesn't declare any functions. This is perfectly reasonable
   * to express in WIT, but not useful for generating bindings. It also triggers name collisions
   * because some WASI worlds import multiple 'types' interfaces.
   */
  context(context: Context)
  private fun IoWorld.Api.worldApiToIr(): IrWorld.Api? {
    return when (this) {
      is IoExternalApi -> externalUsePathToIr()
        .takeIf { getInterfaceOrNull(it.path.usePath)?.declaresApis() ?: false }

      is IoFunction -> functionToIr()
      is IoInterface -> interfaceToIr(context.packageName)
    }
  }

  private fun IncludedWorld.collectIncludesRecursively(
    set: MutableSet<IncludedWorld>,
  ) {
    if (!set.add(this)) return // Duplicate.

    for (include in world.items.filterIsInstance<IoInclude>()) {
      val packageName = include.path.packageName ?: packageName
      val lookupPath = include.path.copy(
        packageName = packageName,
      )
      val world = getWorldOrNull(lookupPath)
        ?: error("unable to find world $lookupPath included by $this")

      IncludedWorld(
        packageName = packageName,
        world = world,
      ).collectIncludesRecursively(
        set = set,
      )
    }
  }

  internal fun getWorldOrNull(path: UsePath): IoWorld? {
    val witPackage = packageNameToPackage[path.packageName] ?: return null
    return witPackage.files.values
      .flatMap { it.items }
      .filterIsInstance<IoWorld>()
      .singleOrNull { it.name == path.name }
  }

  internal fun getInterfaceOrNull(path: UsePath): IoInterface? {
    val witPackage = packageNameToPackage[path.packageName] ?: return null
    return witPackage.files.values
      .flatMap { it.items }
      .filterIsInstance<IoInterface>()
      .singleOrNull { it.name == path.name }
  }

  internal class Context(
    val packageName: PackageName,
    val parentName: Identifier,
  ) {
    override fun toString() = UsePath(packageName, parentName).toString()
  }

  private data class IncludedWorld(
    val packageName: PackageName,
    val world: IoWorld,
  ) {
    val context: Context
      get() = Context(packageName, world.name)

    override fun toString() = context.toString()
  }
}

private fun IoUse.Item.matches(typeName: IoTypeName.Declared): Boolean {
  return when {
    alias != null -> alias == typeName.name
    else -> type == typeName
  }
}

/**
 * Returns true if this interface declares functions to be imported or exported.
 *
 * TODO: probably also return true if any [IoResource] member has a static function or constructor.
 */
private fun IoInterface.declaresApis(): Boolean =
  items.any { it is IoFunction }
