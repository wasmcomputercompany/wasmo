package com.wasmo.support.wit.io

import com.wasmo.support.wit.Documentation
import com.wasmo.support.wit.WitException
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
  fun read(directory: Path): IoWitPackage {
    val files = mutableMapOf<Path, IoWitFile>()
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
          offset = e.offset,
        )
      }
    }

    val packageNames = files.values.mapNotNull { it.packageName }.toSet()
    checkWit(packageNames.size == 1) {
      when {
        packageNames.isEmpty() -> "no package declaration in $directory/*.wit"
        else -> {
          """
          |multiple different package names in $directory/*.wit:
          |  ${packageNames.sorted().joinToString(separator = "\n  ")}
          """.trimMargin()
        }
      }
    }

    return IoWitPackage(
      packageDocumentation = files.values.mapNotNull { it.packageDocumentation }.concatenate(),
      packageName = packageNames.single(),
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
