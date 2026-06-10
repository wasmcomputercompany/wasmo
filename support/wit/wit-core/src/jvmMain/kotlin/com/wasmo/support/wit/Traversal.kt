package com.wasmo.support.wit

/**
 * Returns a sequence that traverses the declarations of this package.
 */
val WitPackage.depthFirstDeclarations: Sequence<Pair<Location, Declaration>>
  get() = sequence {
    for ((path, witFile) in files) {
      for (declaration in witFile.declarations) {
        depthFirstDeclarations(
          location = Location(
            offset = declaration.offset,
            path = path,
            packageName = packageName,
          ),
          subject = declaration,
        )
      }
    }
  }

suspend fun SequenceScope<Pair<Location, Declaration>>.depthFirstDeclarations(
  location: Location,
  subject: Declaration,
) {
  var location = location.copy(offset = subject.offset)
  yield(location to subject)

  when (subject) {
    is Include -> {
      location = location.copy(subject.path)
      for (item in subject.items) {
        yield(location to item)
      }
    }

    is Interface -> {
      location = location.copy(interfaceName = subject.name)
      for (declaration in subject.declarations) {
        depthFirstDeclarations(location, declaration)
      }
    }

    is Package -> {
      location = location.copy(packageName = subject.name)
      for (declaration in subject.declarations) {
        depthFirstDeclarations(location, declaration)
      }
    }

    is Record -> {
      for (field in subject.fields) {
        depthFirstDeclarations(location, field)
      }
    }

    is Resource -> {
      for (function in subject.functions) {
        depthFirstDeclarations(location, function)
      }
    }

    is Variant -> {
      for (case in subject.cases) {
        depthFirstDeclarations(location, case)
      }
    }

    is Use -> {
      location = location.copy(subject.path)
      for (item in subject.items) {
        yield(location to item)
      }
    }

    is World -> {
      location = location.copy(interfaceName = subject.name)
      for (export in subject.declarations) {
        depthFirstDeclarations(location, export)
      }
      for (export in subject.imports) {
        depthFirstDeclarations(location, export)
      }
      for (export in subject.exports) {
        depthFirstDeclarations(location, export)
      }
    }

    else -> {}
  }
}
