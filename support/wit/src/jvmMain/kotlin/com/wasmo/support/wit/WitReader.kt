@file:OptIn(ExperimentalContracts::class)

package com.wasmo.support.wit

import com.wasmo.support.wit.Keywords.deprecated
import com.wasmo.support.wit.Keywords.feature
import com.wasmo.support.wit.Keywords.since
import com.wasmo.support.wit.Keywords.unstable
import com.wasmo.support.wit.Keywords.version
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

        else -> errorWit(location, "unexpected identifier: $identifier")
      }
    }

    return WitFile(
      packageDocumentation = packageDocumentation,
      packageName = packageName,
      declarations = listOf(),
    )
  }

  /**
   * ```ebnf
   * gate ::= gate-item*
   * gate-item ::= unstable-gate
   *             | since-gate
   *             | deprecated-gate
   *
   * unstable-gate ::= '@unstable' '(' feature-field ')'
   * since-gate ::= '@since' '(' version-field ')'
   * deprecated-gate ::= '@deprecated' '(' version-field ')'
   *
   * feature-field ::= 'feature' '=' id
   * version-field ::= 'version' '=' <valid semver>
   * ```
   */
  internal fun readGateOrNull(): Gate? {
    var unstableFeature: Identifier? = null
    var sinceVersion: SemVer? = null
    var deprecatedVersion: SemVer? = null

    while (true) {
      val location = source.location
      val gateItem = source.readAnnotationOrNull() ?: break

      source.skipWhitespace()
      source.readLiteral('(')

      source.skipWhitespace()
      val fieldName = source.readIdentifier()

      source.skipWhitespace()
      source.readLiteral('=')

      source.skipWhitespace()
      when {
        unstableFeature == null && gateItem == unstable && fieldName == feature -> {
          unstableFeature = source.readIdentifier()
        }
        sinceVersion == null && gateItem == since && fieldName == version -> {
          sinceVersion = source.readSemVer()
        }
        deprecatedVersion == null && gateItem == deprecated && fieldName == version -> {
          deprecatedVersion = source.readSemVer()
        }
        else -> errorWit(location, "unexpected field: $gateItem.$fieldName")
      }

      source.skipWhitespace()
      source.readLiteral(')')

      source.skipWhitespace()
    }

    if (unstableFeature == null && sinceVersion == null && deprecatedVersion == null) return null

    return Gate(
      unstable = unstableFeature,
      since = sinceVersion,
      deprecated = deprecatedVersion,
    )
  }
}

object Keywords {
  val `package` = Identifier("package")
  val deprecated = Identifier("deprecated")
  val feature = Identifier("feature")
  val since = Identifier("since")
  val unstable = Identifier("unstable")
  val version = Identifier("version")
}
