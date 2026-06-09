package com.wasmo.support.wit

/**
 * Returns a sequence that traverses the declarations of this package.
 */
val WitPackage.depthFirstDeclarations: Sequence<Pair<Location, Declaration>>
  get() = sequence {
    for ((path, witFile) in files) {
      for (declaration in witFile.declarations) {
        this.depthFirstDeclarations(
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
  var subjectLocation = location.copy(
    offset = subject.offset,
  )
  when (subject) {
    is Include -> {
      subjectLocation = subjectLocation.copy(
        packageName = subject.path.packageName ?: location.packageName,
        interfaceName = subject.path.name,
      )
      yield(subjectLocation to subject)
    }

    is Interface -> {
      subjectLocation = subjectLocation.copy(
        interfaceName = subject.name,
      )
      yield(subjectLocation to subject)
      for (declaration in subject.declarations) {
        depthFirstDeclarations(subjectLocation, declaration)
      }
    }

    is Package -> {
      subjectLocation = subjectLocation.copy(
        packageName = subject.name ?: location.packageName,
      )
      yield(subjectLocation to subject)
      for (declaration in subject.declarations) {
        depthFirstDeclarations(subjectLocation, declaration)
      }
    }

    is Record -> {
      yield(subjectLocation to subject)
      for (field in subject.fields) {
        depthFirstDeclarations(subjectLocation, field)
      }
    }

    is Resource -> {
      yield(subjectLocation to subject)
      for (function in subject.functions) {
        depthFirstDeclarations(subjectLocation, function)
      }
    }

    is Variant -> {
      yield(subjectLocation to subject)
      for (case in subject.cases) {
        depthFirstDeclarations(subjectLocation, case)
      }
    }

    is Use -> {
      subjectLocation = subjectLocation.copy(
        packageName = subject.path.packageName ?: location.packageName,
        interfaceName = subject.path.name,
      )
      yield(subjectLocation to subject)
    }

    is World -> {
      subjectLocation = subjectLocation.copy(
        interfaceName = subject.name,
      )
      yield(subjectLocation to subject)
      for (export in subject.declarations) {
        depthFirstDeclarations(subjectLocation, export)
      }
      for (export in subject.imports) {
        depthFirstDeclarations(subjectLocation, export)
      }
      for (export in subject.exports) {
        depthFirstDeclarations(subjectLocation, export)
      }
    }

    else -> {
      yield(subjectLocation to subject)
    }
  }
}

