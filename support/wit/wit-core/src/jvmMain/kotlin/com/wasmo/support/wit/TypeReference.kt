package com.wasmo.support.wit

/**
 * A reference to a type expected to be found elsewhere.
 *
 * The [location] describes where to look, which is not necessarily where the type was encountered.
 * For example, a [Use] may specify which package to look in for a referenced type.
 */
data class TypeReference(
  val location: Location,
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
  for ((location, subject) in depthFirstDeclarations) {
    when (subject) {
      is Case -> {
        if (subject.type != null) {
          yield(TypeReference(location, subject.type))
        }
      }

      is Field -> yield(TypeReference(location, subject.type))
      is Function -> {
        for (parameter in subject.parameters) {
          yield(TypeReference(location, parameter.type))
        }
        if (subject.returnType != null) {
          yield(TypeReference(location, subject.returnType))
        }
      }

      is Include.Item -> yield(TypeReference(location, subject.type))
      is TypeAlias -> yield(TypeReference(location, subject.target))
      is Use.Item -> yield(TypeReference(location, subject.type))

      else -> {}
    }
  }
}
