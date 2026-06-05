package com.wasmo.support.wit

import okio.FileSystem
import okio.Path

/**
 * Read all of the `.wit` files in a single directory, which collectively make up a package.
 *
 * A package name may be declared in any file in the directory; that name applies to all `.wit`
 * files in the same directory. If multiple files in the same directory declare a package, they must
 * declare the same package.
 */
class WitPackageReader(
  private val fileSystem: FileSystem,
) {
  fun read(directory: Path): WitPackage {
    val files = mutableMapOf<Path, WitFile>()
    for (path in fileSystem.list(directory)) {
      if (!path.name.endsWith(".wit", ignoreCase = true)) continue

      val relativePath = path.relativeTo(directory)
      try {
        files[relativePath] = fileSystem.read(path) {
          readUtf8().toWitFile()
        }
      } catch (e: WitException) {
        throw WitException(
          issue = e.issue,
          path = path.toString(),
          location = e.location,
        )
      }
    }

    val packageNames = files.values.mapNotNull { it.packageName }
    checkWit(packageNames.size <= 1) {
      """
      |multiple different package names in the same directory:
      |  ${packageNames.joinToString(separator = "\n  ")}
      """.trimMargin()
    }

    return WitPackage(
      packageDocumentation = files.values.mapNotNull { it.packageDocumentation }.concatenate(),
      packageName = packageNames.firstOrNull(),
      files = files,
    )
  }

  private fun List<Documentation>.concatenate(): Documentation? {
    return when {
      isNotEmpty() -> Documentation(joinToString(separator = "\n") { it.content })
      else -> null
    }
  }
}
