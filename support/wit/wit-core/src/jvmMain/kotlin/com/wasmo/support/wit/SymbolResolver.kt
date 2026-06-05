package com.wasmo.support.wit

/**
 * Finds fully-qualified WIT types, honoring the following features:
 *
 *  * [TopLevelUse]
 *  * [Use]
 */
class SymbolResolver(
  packages: List<WitPackage>,
) {
  private val packageNameToPackage = packages.associateBy { it.packageName }

  fun resolveType(
    typeName: TypeName.Declared,
    inPackageName: PackageName? = null,
    inInterfaceName: Identifier? = null,
  ): TypePath {
    return resolveTypeOrNull(typeName, inPackageName, inInterfaceName)
      ?: throw IllegalArgumentException(
        buildString {
          append("unable to resolve $typeName")
          when {
            inInterfaceName != null -> append(" in ${UsePath(inPackageName, inInterfaceName)}")
            inPackageName != null -> append(" in $inPackageName")
          }
        },
      )
  }

  fun resolveTypeOrNull(
    typeName: TypeName.Declared,
    inPackageName: PackageName? = null,
    inInterfaceName: Identifier? = null,
  ): TypePath? {
    val witPackage = packageNameToPackage[inPackageName] ?: return null

    for (witFile in witPackage.files.values) {
      for (`interface` in witFile.declarations) {
        if (`interface` !is Interface || `interface`.name != inInterfaceName) continue

        for (declaration in `interface`.declarations) {
          when (declaration) {
            is TypeDeclaration -> {
              // Direct match.
              if (declaration.name == typeName.name) {
                return TypePath(witPackage.packageName, inInterfaceName, declaration.name)
              }
            }

            is Use -> {
              // Matched a 'use' statement that refers to another symbol.
              val itemMatch = declaration.items.firstOrNull { it.matches(typeName) }
              if (itemMatch != null) {
                return resolveTypeOrNull(
                  itemMatch.type,
                  declaration.path.packageName ?: inPackageName,
                  declaration.path.name,
                )
              }
            }

            else -> {}
          }
        }
      }
    }

    return null
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
