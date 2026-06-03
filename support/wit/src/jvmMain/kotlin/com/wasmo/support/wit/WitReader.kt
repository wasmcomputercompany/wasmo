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

    return when (val identifier = source.readIdentifier()) {
      Keywords.enum -> readEnum(documentation, gate, location)
      Keywords.flags -> readFlags(documentation, gate, location)
      Keywords.record -> readRecord(documentation, gate, location)
      Keywords.resource -> readResource(documentation, gate, location)
      Keywords.variant -> readVariant(documentation, gate, location)
      Keywords.type -> readTypeAlias(documentation, gate, location)
      Keywords.use -> readUse(documentation, gate, location)
      else -> readFunction(documentation, gate, location, identifier)
    }
  }

  /**
   * ```ebnf
   * record-item ::= 'record' id '{' record-fields '}'
   *
   * record-fields ::= record-field
   *                 | record-field ',' record-fields?
   *
   * record-field ::= id ':' ty
   * ```
   */
  private fun readRecord(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): Record {
    source.skipWhitespace()
    val name = source.readIdentifier()

    val fields = mutableListOf<Field>()

    source.skipWhitespace()
    source.readLiteral('{')

    source.skipWhitespace()
    while (true) {
      val fieldGate = readGateOrNull()
      val fieldDocumentation = source.takeDocumentation()
      val fieldLocation = source.location
      val fieldName = source.readIdentifier()

      source.skipWhitespace()
      source.readLiteral(':')

      source.skipWhitespace()
      val fieldType = source.readTypeName()

      fields += Field(
        documentation = fieldDocumentation,
        gate = fieldGate,
        location = fieldLocation,
        name = fieldName,
        typeName = fieldType,
      )

      source.skipWhitespace()
      if (source.tryReadLiteral(',')) {
        source.skipWhitespace()
        if (source.tryReadLiteral('}')) break // Trailing comma.
        continue
      }

      source.readLiteral('}')
      break
    }

    return Record(
      documentation = documentation,
      gate = gate,
      location = location,
      name = TypeName(name),
      fields = fields,
    )
  }

  /**
   * ```ebnf
   * variant-items ::= 'variant' id '{' variant-cases '}'
   *
   * variant-cases ::= variant-case
   *                 | variant-case ',' variant-cases?
   *
   * variant-case ::= id
   *                | id '(' ty ')'
   * ```
   */
  private fun readVariant(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): Variant {
    source.skipWhitespace()
    val name = source.readIdentifier()

    val cases = mutableListOf<Case>()

    source.skipWhitespace()
    source.readLiteral('{')

    source.skipWhitespace()
    while (true) {
      val caseGate = readGateOrNull()
      val caseDocumentation = source.takeDocumentation()
      val caseLocation = source.location
      val caseName = source.readIdentifier()

      source.skipWhitespace()
      val typeName = when {
        source.tryReadLiteral('(') -> {
          source.readTypeName()
            .also {
              source.skipWhitespace()
              source.readLiteral(')')
            }

        }

        else -> null
      }

      cases += Case(
        documentation = caseDocumentation,
        gate = caseGate,
        location = caseLocation,
        name = caseName,
        typeName = typeName,
      )

      source.skipWhitespace()
      if (source.tryReadLiteral(',')) {
        source.skipWhitespace()
        if (source.tryReadLiteral('}')) break // Trailing comma.
        continue
      }

      source.readLiteral('}')
      break
    }

    return Variant(
      documentation = documentation,
      gate = gate,
      location = location,
      name = TypeName(name),
      cases = cases,
    )
  }

  /**
   * ```ebnf
   * resource-item ::= 'resource' id ';'
   *                 | 'resource' id '{' resource-method* '}'
   * resource-method ::= func-item
   *                   | id ':' 'static' func-type ';'
   *                   | 'constructor' param-list ';'
   * ```
   */
  private fun readResource(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): Resource {
    source.skipWhitespace()
    val name = TypeName(source.readIdentifier())
    val declarations = mutableListOf<Function>()

    source.skipWhitespace()
    if (source.tryReadLiteral('{')) {
      while (true) {
        source.skipWhitespace()
        if (source.tryReadLiteral('}')) break

        val functionGate = readGateOrNull()
        val functionDocumentation = source.takeDocumentation()
        val functionLocation = source.location

        when (val identifier = source.readIdentifier()) {
          Keywords.constructor -> {
            val parameters = readParameterList()
            source.skipWhitespace()
            source.readLiteral(';')
            declarations += Function(
              documentation = functionDocumentation,
              gate = functionGate,
              location = functionLocation,
              constructor = true,
              name = identifier,
              parameters = parameters,
            )
          }

          else -> {
            declarations += readFunction(
              documentation = functionDocumentation,
              gate = functionGate,
              location = functionLocation,
              identifier = identifier,
            )
          }
        }
      }
    } else {
      source.readLiteral(';')
    }

    return Resource(
      documentation = documentation,
      gate = gate,
      location = location,
      name = name,
      declarations = declarations,
    )
  }

  /**
   * ```ebnf
   * flags-items ::= 'flags' id '{' flags-fields '}'
   *
   * flags-fields ::= id
   *                | id ',' flags-fields?
   * ```
   */
  private fun readFlags(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): Flags {
    source.skipWhitespace()
    val name = source.readIdentifier()

    val flags = mutableListOf<Flag>()

    source.skipWhitespace()
    source.readLiteral('{')

    source.skipWhitespace()
    while (true) {
      val flagGate = readGateOrNull()
      val flagDocumentation = source.takeDocumentation()
      val flagLocation = source.location
      val flagName = source.readIdentifier()

      flags += Flag(
        documentation = flagDocumentation,
        gate = flagGate,
        location = flagLocation,
        name = flagName,
      )

      source.skipWhitespace()
      if (source.tryReadLiteral(',')) {
        source.skipWhitespace()
        if (source.tryReadLiteral('}')) break // Trailing comma.
        continue
      }

      source.readLiteral('}')
      break
    }

    return Flags(
      documentation = documentation,
      gate = gate,
      location = location,
      name = TypeName(name),
      flags = flags,
    )
  }

  /**
   * ```ebnf
   * enum-items ::= 'enum' id '{' enum-cases '}'
   *
   * enum-cases ::= id
   *              | id ',' enum-cases?
   * ```
   */
  private fun readEnum(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): Enum {
    source.skipWhitespace()
    val name = source.readIdentifier()

    val cases = mutableListOf<Case>()

    source.skipWhitespace()
    source.readLiteral('{')

    source.skipWhitespace()
    while (true) {
      val caseGate = readGateOrNull()
      val caseDocumentation = source.takeDocumentation()
      val caseLocation = source.location
      val caseName = source.readIdentifier()

      cases += Case(
        documentation = caseDocumentation,
        gate = caseGate,
        location = caseLocation,
        name = caseName,
      )

      source.skipWhitespace()
      if (source.tryReadLiteral(',')) {
        source.skipWhitespace()
        if (source.tryReadLiteral('}')) break // Trailing comma.
        continue
      }

      source.readLiteral('}')
      break
    }

    return Enum(
      documentation = documentation,
      gate = gate,
      location = location,
      name = TypeName(name),
      cases = cases,
    )
  }

  /**
   * ```ebnf
   * type-item ::= 'type' id '=' ty ';'
   * ```
   */
  private fun readTypeAlias(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): TypeAlias {
    source.skipWhitespace()
    val name = source.readIdentifier()

    source.skipWhitespace()
    source.readLiteral('=')

    source.skipWhitespace()
    val type = source.readTypeName()

    source.skipWhitespace()
    source.readLiteral(';')

    return TypeAlias(
      documentation = documentation,
      gate = gate,
      location = location,
      name = TypeName(name),
      target = type,
    )
  }

  /**
   * ```ebnf
   * use-item ::= 'use' use-path '.' '{' use-names-list '}' ';'
   *
   * use-names-list ::= use-names-item
   *                  | use-names-item ',' use-names-list?
   *
   * use-names-item ::= id
   *                  | id 'as' id
   * ```
   */
  private fun readUse(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): Use {
    source.skipWhitespace()
    val usePath = source.readUsePath()

    source.skipWhitespace()
    source.readLiteral('.')
    source.skipWhitespace()
    source.readLiteral('{')

    val items = mutableListOf<Use.Item>()

    source.skipWhitespace()
    while (true) {
      val itemName = source.readIdentifier()

      source.skipWhitespace()
      val alias = when {
        source.tryReadLiteral("as") -> {
          source.skipWhitespace()
          source.readIdentifier()
        }

        else -> null
      }

      items += Use.Item(
        name = itemName,
        alias = alias,
      )

      source.skipWhitespace()
      if (source.tryReadLiteral(',')) {
        source.skipWhitespace()
        if (source.tryReadLiteral('}')) break // Trailing comma.
        continue
      }

      source.readLiteral('}')
      break
    }

    return Use(
      documentation = documentation,
      gate = gate,
      location = location,
      path = usePath,
      items = items,
    )
  }

  /**
   * ```ebnf
   * func-item ::= id ':' func-type ';'
   *
   * func-type ::= 'async'? 'func' param-list result-list
   *
   * result-list ::= ϵ
   *               | '->' ty
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
    var static = false

    while (true) {
      source.skipWhitespace()
      when (val modifier = source.readIdentifier()) {
        Keywords.async -> async = true
        Keywords.static -> static = true
        Keywords.func -> break
        else -> errorWit(location, "unexpected identifier: $modifier")
      }
    }

    val parameters = readParameterList()

    source.skipWhitespace()
    val returnType = when {
      source.tryReadLiteral("->") -> {
        source.skipWhitespace()
        source.readTypeName()
          .also { source.skipWhitespace() }
      }

      else -> null
    }

    source.skipWhitespace()
    source.readLiteral(';')

    return Function(
      documentation = documentation,
      gate = gate,
      location = location,
      async = async,
      static = static,
      constructor = false,
      name = identifier,
      parameters = parameters,
      returnType = returnType,
    )
  }

  /**
   * ```ebnf
   * param-list ::= '(' named-type-list ')'
   *
   * named-type-list ::= ϵ
   *                   | named-type ( ',' named-type )*
   *
   * named-type ::= id ':' ty
   * ```
   */
  private fun readParameterList(): List<Parameter> {
    source.readLiteral('(')

    val result = mutableListOf<Parameter>()
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

      result += Parameter(
        location = location,
        name = parameterName,
        typeName = parameterType,
      )
    }

    return result
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
  val constructor = Identifier("constructor")
  val deprecated = Identifier("deprecated")
  val enum = Identifier("enum")
  val feature = Identifier("feature")
  val flags = Identifier("flags")
  val func = Identifier("func")
  val future = Identifier("future")
  val list = Identifier("list")
  val map = Identifier("map")
  val option = Identifier("option")
  val record = Identifier("record")
  val resource = Identifier("resource")
  val result = Identifier("result")
  val since = Identifier("since")
  val static = Identifier("static")
  val stream = Identifier("stream")
  val tuple = Identifier("tuple")
  val type = Identifier("type")
  val unstable = Identifier("unstable")
  val use = Identifier("use")
  val variant = Identifier("variant")
  val version = Identifier("version")
}
