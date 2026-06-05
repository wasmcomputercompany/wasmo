package com.wasmo.support.wit

/**
 * Finds fully-qualified WIT types, honoring the following features:
 *
 *  * [TopLevelUse]
 *  * [Use]
 */
class SymbolResolver(
  private val packages: List<WitPackage>,
) {
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
    for (witPackage in packages) {
      if (witPackage.packageName != inPackageName) continue
      for (witFile in witPackage.files.values) {
        for (`interface` in witFile.declarations) {
          if (`interface` !is Interface || `interface`.name != inInterfaceName) continue

          for (type in `interface`.declarations) {
            if (type !is TypeDeclaration) continue
            if (type.name == typeName.name) {
              return TypePath(witPackage.packageName, inInterfaceName, type.name)
            }
          }
        }
      }
    }

    return null
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
