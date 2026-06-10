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

  fun getType(scope: Scope, typeName: TypeName.Declared): TypePath {
    return getTypeOrNull(scope, typeName)
      ?: throw IllegalArgumentException("unable to find $typeName in $scope")
  }

  fun getTypeOrNull(scope: Scope, typeName: TypeName.Declared): TypePath? {
    val witPackage = packageNameToPackage[scope.packageName] ?: return null
    val declarations = witPackage.files.values
      .flatMap { it.declarations }
      .filterIsInstance<Interface>()
      .filter { it.name == scope.interfaceName }
      .flatMap { it.declarations }

    for (declaration in declarations) {
      when (declaration) {
        is TypeDeclaration -> {
          // Direct match.
          if (declaration.name == typeName.name) {
            return TypePath(witPackage.packageName, scope.interfaceName!!, declaration.name)
          }
        }

        is Use -> {
          // Matched a 'use' statement that refers to another symbol.
          val itemMatch = declaration.items.firstOrNull { it.matches(typeName) }
          if (itemMatch != null) {
            return getTypeOrNull(
              scope = scope.copy(usePath = declaration.path),
              typeName = itemMatch.type,
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
  val packageName: PackageName,
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
      packageName = PackageName(
        namespaces = listOf(Identifier(namespace)),
        names = listOf(Identifier(packageName)),
        version = version?.let { SemVer(it) },
      ),
      interfaceName = Identifier(interfaceName),
      typeName = Identifier(typeName),
    )
  }
}
