package com.wasmo.support.wit

import okio.Path

/**
 * Returns a sequence that traverses the declarations of this package.
 */
val WitPackage.depthFirstDeclarations: Sequence<ScopedDeclaration>
  get() = sequence {
    for ((path, witFile) in files) {
      for (declaration in witFile.declarations) {
        depthFirstDeclarations(
          path = path,
          scope = Scope(packageName = packageName),
          subject = declaration,
        )
      }
    }
  }

/**
 * A declaration plus the scope to resolve its references.
 */
data class ScopedDeclaration(
  val path: Path,
  val scope: Scope,
  val declaration: Declaration,
)

private suspend fun SequenceScope<ScopedDeclaration>.depthFirstDeclarations(
  path: Path,
  scope: Scope,
  subject: Declaration,
) {
  yield(ScopedDeclaration(path, scope, subject))

  when (subject) {
    is Include -> {
      val scope = scope.copy(usePath = subject.path)
      for (item in subject.items) {
        yield(ScopedDeclaration(path, scope, item))
      }
    }

    is Interface -> {
      val scope = scope.copy(interfaceName = subject.name)
      for (declaration in subject.declarations) {
        depthFirstDeclarations(path, scope, declaration)
      }
    }

    is Package -> {
      val scope = Scope(packageName = subject.name, interfaceName = null)
      for (declaration in subject.declarations) {
        depthFirstDeclarations(path, scope, declaration)
      }
    }

    is Record -> {
      for (field in subject.fields) {
        depthFirstDeclarations(path, scope, field)
      }
    }

    is Resource -> {
      for (function in subject.functions) {
        depthFirstDeclarations(path, scope, function)
      }
    }

    is Variant -> {
      for (case in subject.cases) {
        depthFirstDeclarations(path, scope, case)
      }
    }

    is Use -> {
      val scope = scope.copy(usePath = subject.path)
      for (item in subject.items) {
        yield(ScopedDeclaration(path, scope, item))
      }
    }

    is World -> {
      val scope = scope.copy(interfaceName = subject.name)
      for (declaration in subject.declarations) {
        depthFirstDeclarations(path, scope, declaration)
      }
      for (import in subject.imports) {
        depthFirstDeclarations(path, scope, import)
      }
      for (export in subject.exports) {
        depthFirstDeclarations(path, scope, export)
      }
    }

    else -> {}
  }
}
