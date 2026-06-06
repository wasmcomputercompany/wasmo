package com.wasmo.support.wit

import okio.Path

/**
 * Returns all type references in this package.
 *
 * Use this to reject broken references independent of code generation. This will find broken
 * references that aren't necessary to generate source code. In particular, unused [Use]
 * declarations don't otherwise impact source code.
 */
fun WitPackage.typeReferences(): Sequence<TypeReference> = sequence {
  for ((path, witFile) in files) {
    for (declaration in witFile.declarations) {
      TypeReferenceScanner(
        path = path,
        packageName = packageName,
        subject = declaration,
      ).scan()
    }
  }
}

/**
 * A reference to a type expected to be found elsewhere.
 *
 * The [packageName] and [interfaceName] describe where to look, which is not necessarily where the
 * type was encountered. For example, a [Use] may specify which package to look in for a referenced
 * type.
 */
data class TypeReference(
  val path: Path,
  val location: Location,
  val packageName: PackageName?,
  val interfaceName: Identifier?,
  val typeName: TypeName,
)

/** Does a depth-first traversal of [subject], looking for [TypeName] instances. */
private class TypeReferenceScanner(
  private val path: Path,
  private val packageName: PackageName?,
  private val interfaceName: Identifier? = null,
  private val subject: Declaration,
) {
  context(sequence: SequenceScope<TypeReference>)
  suspend fun scan() {
    when (subject) {
      is Case -> yield(subject.type)
      is Export -> scan(subject.value)
      is Field -> yield(subject.type)
      is Function -> {
        for (parameter in subject.parameters) {
          yield(parameter.type)
        }
        yield(subject.returnType)
      }

      is Import -> scan(subject.value)
      is Include -> {
        val scanner = scanner(
          interfaceName = subject.path.name,
          packageName = subject.path.packageName ?: packageName,
        )
        for (item in subject.items) {
          scanner.yield(item.type)
        }
      }

      is Interface -> scanner(interfaceName = subject.name).scan(subject.declarations)
      is Package -> scan(subject.declarations)
      is Record -> scan(subject.fields)
      is Resource -> scan(subject.functions)
      is TypeAlias -> yield(subject.target)
      is Variant -> scan(subject.cases)
      is Use -> {
        val scanner = scanner(
          packageName = subject.path.packageName ?: packageName,
          interfaceName = subject.path.name,
        )
        for (item in subject.items) {
          scanner.yield(item.type)
        }
      }

      else -> {}
    }
  }

  /** Returns a new scanner instance, which provides context to yielded types. */
  private fun scanner(
    subject: Declaration = this.subject,
    packageName: PackageName? = this.packageName,
    interfaceName: Identifier? = this.interfaceName,
  ) = TypeReferenceScanner(
    path = path,
    subject = subject,
    packageName = packageName,
    interfaceName = interfaceName,
  )

  context(sequence: SequenceScope<TypeReference>)
  private suspend fun scan(declarations: Iterable<Declaration>) {
    for (declaration in declarations) {
      scanner(subject = declaration).scan()
    }
  }

  context(sequence: SequenceScope<TypeReference>)
  private suspend fun scan(value: ExternalType) {
    return when (value) {
      is Declaration -> scanner(value).scan()
      is ExternalUsePath -> {}
    }
  }

  context(sequence: SequenceScope<TypeReference>)
  private suspend fun yield(type: TypeName?) {
    if (type != null) {
      sequence.yield(TypeReference(path, subject.location, packageName, interfaceName, type))
    }
  }
}
