@file:OptIn(ExperimentalContracts::class)

package com.wasmo.support.wit

import kotlin.contracts.ExperimentalContracts

class WitReader private constructor(
  private val source: WitStructureReader,
) {
  constructor(string: String) : this(WitStructureReader(string))

  fun read(): WitFile {
    var packageDocumentation: Documentation? = null
    var packageName: PackageName? = null

    while (true) {
      source.skipWhitespace()
      if (source.exhausted) break
      val documentation = source.takeDocumentation()
      val location = source.location

      when (val identifier = source.readIdentifier()) {
        Keywords.`package` -> {
          checkWit(location, packageName == null) {
            "multiple package declarations"
          }
          source.skipWhitespace()
          packageDocumentation = documentation
          packageName = source.readPackageName()
          source.readLiteral(';')
        }

        else -> {
          checkWit(location, false) {
            "unexpected identifier: $identifier"
          }
        }
      }
    }

    return WitFile(
      packageDocumentation = packageDocumentation,
      packageName = packageName,
      declarations = listOf(),
    )
  }
}

private object Keywords {
  val `package` = Identifier("package")
}
