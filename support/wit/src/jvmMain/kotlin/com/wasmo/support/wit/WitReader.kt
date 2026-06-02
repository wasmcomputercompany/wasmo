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
    val declarations = mutableListOf<Declaration>()

    var packageDocumentation: Documentation? = null
    var packageName: PackageName? = null

    while (true) {
      source.skipWhitespace()
      if (source.exhausted) break

      val gate = readGateOrNull()
      val documentation = source.takeDocumentation()
      val location = source.location

      when (val identifier = source.readIdentifier()) {
        Keywords.`package` if (packageName == null) -> {
          source.skipWhitespace()
          packageDocumentation = documentation
          packageName = source.readPackageName()
          source.readLiteral(';')
        }

        Keywords.`interface` -> {
          declarations += readInterface(documentation, gate, location)
        }

        else -> errorWit(location, "unexpected identifier: $identifier")
      }
    }

    return WitFile(
      packageDocumentation = packageDocumentation,
      packageName = packageName,
      declarations = declarations,
    )
  }

  /**
   * ```ebnf
   * interface-item ::= gate 'interface' id '{' interface-items* '}'
   * ```
   */
  internal fun readInterface(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): Interface {
    source.skipWhitespace()
    val name = source.readIdentifier()

    source.skipWhitespace()
    source.readLiteral('{')

    val declarations = mutableListOf<Declaration>()

    while (true) {
      source.skipWhitespace()
      if (source.tryReadLiteral('}')) break

      declarations += readInterfaceItem()
    }

    return Interface(
      documentation = documentation,
      gate = gate,
      location = location,
      name = TypeName(name),
      declarations = declarations,
    )
  }

  /**
   * ```ebnf
   * interface-items ::= gate interface-definition
   *
   * interface-definition ::= typedef-item
   *                        | use-item
   *                        | func-item
   *
   * typedef-item ::= resource-item
   *                | variant-items
   *                | record-item
   *                | flags-items
   *                | enum-items
   *                | type-item
   * ```
   */
  internal fun readInterfaceItem(): Declaration {
    val gate = readGateOrNull()
    val documentation = source.takeDocumentation()
    val location = source.location

    when (val identifier = source.readIdentifier()) {
      else -> return readFunction(documentation, gate, location, identifier)
    }
  }

  /**
   * ```ebnf
   * func-item ::= id ':' func-type ';'
   *
   * func-type ::= 'async'? 'func' param-list result-list
   *
   * param-list ::= '(' named-type-list ')'
   *
   * result-list ::= ϵ
   *               | '->' ty
   *
   * named-type-list ::= ϵ
   *                   | named-type ( ',' named-type )*
   *
   * named-type ::= id ':' ty
   * ```
   */
  private fun readFunction(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
    identifier: Identifier,
  ): Function {
    source.skipWhitespace()
    source.readLiteral(':')

    var async = false

    source.skipWhitespace()
    var modifier = source.readIdentifier()
    while (true) {
      if (modifier == Keywords.async) {
        async = true
        source.skipWhitespace()
        modifier = source.readIdentifier()
      } else if (modifier == Keywords.func) {
        break
      } else {
        errorWit(location, "unexpected identifier: $modifier")
      }
    }

    val parameters = mutableListOf<Parameter>()
    source.skipWhitespace()
    source.readLiteral('(')

    var first = true
    while (true) {
      source.skipWhitespace()
      if (source.tryReadLiteral(')')) break

      if (first) {
        first = false
      } else {
        source.readLiteral(',')
      }

      source.skipWhitespace()
      val location = source.location
      val parameterName = source.readIdentifier()

      source.skipWhitespace()
      source.readLiteral(':')

      source.skipWhitespace()
      val parameterType = source.readTypeName()

      parameters += Parameter(
        location = location,
        name = parameterName,
        typeName = parameterType,
      )
    }

    source.skipWhitespace()
    val returnType = when {
      source.tryReadLiteral("->") -> {
        source.skipWhitespace()
        source.readTypeName()
          .also { source.skipWhitespace() }
      }

      else -> null
    }

    source.readLiteral(';')

    return Function(
      documentation = documentation,
      gate = gate,
      location = location,
      async = async,
      static = false,
      constructor = false,
      name = identifier,
      parameters = parameters,
      returnType = returnType,
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
  val `interface` = Identifier("interface")
  val `package` = Identifier("package")
  val async = Identifier("async")
  val borrow = Identifier("borrow")
  val deprecated = Identifier("deprecated")
  val feature = Identifier("feature")
  val func = Identifier("func")
  val future = Identifier("future")
  val list = Identifier("list")
  val map = Identifier("map")
  val option = Identifier("option")
  val result = Identifier("result")
  val since = Identifier("since")
  val stream = Identifier("stream")
  val tuple = Identifier("tuple")
  val unstable = Identifier("unstable")
  val version = Identifier("version")
}
