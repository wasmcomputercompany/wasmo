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
            interfaceName = null,
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
  val subjectLocation = location.copy(offset = subject.offset)
  yield(subjectLocation to subject)

  when (subject) {
    is Include -> {
      val childLocation = subjectLocation.copy(
        packageName = subject.path.packageName ?: location.packageName,
        interfaceName = subject.path.name,
      )
      for (item in subject.items) {
        yield(childLocation to item)
      }
    }

    is Interface -> {
      val childLocation = subjectLocation.copy(
        interfaceName = subject.name,
      )
      for (declaration in subject.declarations) {
        depthFirstDeclarations(childLocation, declaration)
      }
    }

    is Package -> {
      val childLocation = subjectLocation.copy(
        packageName = subject.name ?: location.packageName,
      )
      for (declaration in subject.declarations) {
        depthFirstDeclarations(childLocation, declaration)
      }
    }

    is Record -> {
      for (field in subject.fields) {
        depthFirstDeclarations(subjectLocation, field)
      }
    }

    is Resource -> {
      for (function in subject.functions) {
        depthFirstDeclarations(subjectLocation, function)
      }
    }

    is Variant -> {
      for (case in subject.cases) {
        depthFirstDeclarations(subjectLocation, case)
      }
    }

    is Use -> {
      val childLocation = subjectLocation.copy(
        packageName = subject.path.packageName ?: location.packageName,
        interfaceName = subject.path.name,
      )
      for (item in subject.items) {
        yield(childLocation to item)
      }
    }

    is World -> {
      val childLocation = subjectLocation.copy(
        interfaceName = subject.name,
      )
      for (export in subject.declarations) {
        depthFirstDeclarations(childLocation, export)
      }
      for (export in subject.imports) {
        depthFirstDeclarations(childLocation, export)
      }
      for (export in subject.exports) {
        depthFirstDeclarations(childLocation, export)
      }
    }

    else -> {}
  }
}

