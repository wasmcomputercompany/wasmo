package com.wasmo.support.wit

/**
 * Finds fully-qualified WIT types, honoring the following features:
 *
 *  * [TopLevelUse]
 *  * [Use]
 */
class SymbolIndex(
  packages: List<WitPackage>,
) {
  private val packageNameToPackage = packages.associateBy { it.packageName }

  fun getType(
    typeName: TypeName.Declared,
    inPackageName: PackageName? = null,
    inInterfaceName: Identifier? = null,
  ): TypePath {
    return getTypeOrNull(typeName, inPackageName, inInterfaceName)
      ?: throw IllegalArgumentException(
        buildString {
          append("unable to find $typeName")
          when {
            inInterfaceName != null -> append(" in ${UsePath(inPackageName, inInterfaceName)}")
            inPackageName != null -> append(" in $inPackageName")
          }
        },
      )
  }

  fun getTypeOrNull(
    typeName: TypeName.Declared,
    inPackageName: PackageName? = null,
    inInterfaceName: Identifier? = null,
  ): TypePath? {
    val witPackage = packageNameToPackage[inPackageName] ?: return null
    val declarations = witPackage.files.values
      .flatMap { it.declarations }
      .filterIsInstance<Interface>()
      .filter { it.name == inInterfaceName }
      .flatMap { it.declarations }

    for (declaration in declarations) {
      when (declaration) {
        is TypeDeclaration -> {
          // Direct match.
          if (declaration.name == typeName.name) {
            return TypePath(witPackage.packageName, inInterfaceName!!, declaration.name)
          }
        }

        is Use -> {
          // Matched a 'use' statement that refers to another symbol.
          val itemMatch = declaration.items.firstOrNull { it.matches(typeName) }
          if (itemMatch != null) {
            return getTypeOrNull(
              itemMatch.type,
              declaration.path.packageName ?: inPackageName,
              declaration.path.name,
            )
          }
        }

        else -> {}
      }
    }

    return null
  }

  fun getWorldOrNull(path: UsePath): World? {
    val witPackage = packageNameToPackage[path.packageName] ?: return null
    return witPackage.files.values
      .flatMap { it.declarations }
      .filterIsInstance<World>()
      .singleOrNull { it.name == path.name }
  }
}

private fun Use.Item.matches(typeName: TypeName.Declared): Boolean {
  return when {
    alias != null -> alias == typeName.name
    else -> type == typeName
  }
}

/** A fully-qualified type name. */
data class TypePath(
  val packageName: PackageName? = null,
  val interfaceName: Identifier,
  val typeName: Identifier,
) {
  override fun toString(): String = "${UsePath(packageName, interfaceName)}.{$typeName}"

  companion object {
    operator fun invoke(
      namespace: String,
      packageName: String,
      interfaceName: String,
      typeName: String,
      version: String? = null,
    ) = TypePath(
      packageName = PackageName(namespace, packageName, version),
      interfaceName = Identifier(interfaceName),
      typeName = Identifier(typeName),
    )
  }
}
