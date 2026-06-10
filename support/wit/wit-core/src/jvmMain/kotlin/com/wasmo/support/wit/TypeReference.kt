package com.wasmo.support.wit

import okio.Path

/**
 * A reference to a type expected to be found elsewhere.
 *
 * The [scope] describes where to look, which is not necessarily where the type was encountered.
 * For example, a [Use] may specify which package to look in for a referenced type.
 */
data class TypeReference(
  val path: Path,
  val offset: Offset,
  val scope: Scope,
  val typeName: TypeName,
)

/**
 * Returns all type references in this package.
 *
 * Use this to reject broken references independent of code generation. This will find broken
 * references that aren't necessary to generate source code. In particular, unused [Use]
 * declarations don't otherwise impact source code.
 */
fun WitPackage.typeReferences(): Sequence<TypeReference> = sequence {
  for (declaration in depthFirstDeclarations) {
    when (val subject = declaration.declaration) {
      is Case -> {
        if (subject.type != null) {
          yield(declaration.typeReference(subject.type))
        }
      }

      is Field -> yield(declaration.typeReference(subject.type))
      is Function -> {
        for (parameter in subject.parameters) {
          yield(declaration.typeReference(parameter.type))
        }
        if (subject.returnType != null) {
          yield(declaration.typeReference(subject.returnType))
        }
      }

      is Include.Item -> yield(declaration.typeReference(subject.type))
      is TypeAlias -> yield(declaration.typeReference(subject.target))
      is Use.Item -> yield(declaration.typeReference(subject.type))

      else -> {}
    }
  }
}

private fun ScopedDeclaration.typeReference(typeName: TypeName) = TypeReference(
  path = path,
  offset = declaration.offset,
  scope = scope,
  typeName = typeName,
)
