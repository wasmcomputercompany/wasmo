package com.wasmo.support.wit

import okio.Path

/**
 * Returns a sequence that traverses the declarations of this package.
 */
val WitPackage.depthFirstDeclarations: Sequence<LocatedDeclaration>
  get() = sequence {
    for ((path, witFile) in files) {
      for (declaration in witFile.declarations) {
        depthFirstDeclarations(
          path = path,
          location = Location(packageName = packageName),
          subject = declaration,
        )
      }
    }
  }

data class LocatedDeclaration(
  val path: Path,
  val location: Location,
  val declaration: Declaration,
)

private suspend fun SequenceScope<LocatedDeclaration>.depthFirstDeclarations(
  path: Path,
  location: Location,
  subject: Declaration,
) {
  yield(LocatedDeclaration(path, location, subject))

  when (subject) {
    is Include -> {
      val location = location.copy(usePath = subject.path)
      for (item in subject.items) {
        yield(LocatedDeclaration(path, location, item))
      }
    }

    is Interface -> {
      val location = location.copy(interfaceName = subject.name)
      for (declaration in subject.declarations) {
        depthFirstDeclarations(path, location, declaration)
      }
    }

    is Package -> {
      val location = location.copy(packageName = subject.name, interfaceName = null)
      for (declaration in subject.declarations) {
        depthFirstDeclarations(path, location, declaration)
      }
    }

    is Record -> {
      for (field in subject.fields) {
        depthFirstDeclarations(path, location, field)
      }
    }

    is Resource -> {
      for (function in subject.functions) {
        depthFirstDeclarations(path, location, function)
      }
    }

    is Variant -> {
      for (case in subject.cases) {
        depthFirstDeclarations(path, location, case)
      }
    }

    is Use -> {
      val location = location.copy(usePath = subject.path)
      for (item in subject.items) {
        yield(LocatedDeclaration(path, location, item))
      }
    }

    is World -> {
      val location = location.copy(interfaceName = subject.name)
      for (export in subject.declarations) {
        depthFirstDeclarations(path, location, export)
      }
      for (export in subject.imports) {
        depthFirstDeclarations(path, location, export)
      }
      for (export in subject.exports) {
        depthFirstDeclarations(path, location, export)
      }
    }

    else -> {}
  }
}
