package com.wasmo.support.wit

import com.wasmo.support.wit.Keywords.deprecated
import com.wasmo.support.wit.Keywords.feature
import com.wasmo.support.wit.Keywords.since
import com.wasmo.support.wit.Keywords.unstable
import com.wasmo.support.wit.Keywords.version

fun String.toWitFile(): WitFile = WitFileReader(this).read()

internal class WitFileReader(
  private val source: WitStructureReader,
) {
  constructor(string: String) : this(WitStructureReader(string))

  fun read(): WitFile {
    val declarations = mutableListOf<Declaration>()

    var packageIdentifier: Package? = null

    while (true) {
      source.skipWhitespace()
      if (source.exhausted) break

      val gate = readGateOrNull()
      val documentation = source.takeDocumentation()
      val location = source.location

      when (val identifier = source.readIdentifier()) {
        Keywords.`package` -> {
          val (value, kind) = readPackage(documentation, gate, location)
          when (kind) {
            PackageKind.Identifier -> {
              checkWit(packageIdentifier == null && declarations.isEmpty(), location) {
                "unexpected package identifier"
              }
              packageIdentifier = value
            }

            PackageKind.Nested -> {
              declarations += value
            }
          }
        }

        Keywords.`interface` -> {
          declarations += readInterface(documentation, gate, location)
        }

        Keywords.use -> {
          declarations += readTopLevelUse(documentation, gate, location)
        }

        Keywords.world -> {
          declarations += readWorld(documentation, gate, location)
        }

        else -> errorWit(location, "unexpected identifier: $identifier")
      }
    }

    return WitFile(
      packageDocumentation = packageIdentifier?.documentation,
      packageName = packageIdentifier?.name,
      declarations = declarations,
    )
  }

  /**
   * Reads either a package identifier or a nested package.
   *
   * ```ebnf
   * nested-package-definition ::= package-decl '{' package-items* '}'
   * package-items ::= toplevel-use-item | interface-item | world-item
   * ```
   */
  private fun readPackage(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): Pair<Package, PackageKind> {
    source.skipWhitespace()
    val name = source.readPackageName()
    val declarations = mutableListOf<Declaration>()

    source.skipWhitespace()
    val packageKind = when {
      source.tryReadLiteral('{') -> {
        while (true) {
          source.skipWhitespace()
          if (source.tryReadLiteral('}')) break

          val nestedGate = readGateOrNull()
          val nestedDocumentation = source.takeDocumentation()
          val nestedLocation = source.location

          declarations += when (val identifier = source.readIdentifier()) {
            Keywords.`interface` -> readInterface(nestedDocumentation, nestedGate, nestedLocation)
            Keywords.use -> readTopLevelUse(nestedDocumentation, nestedGate, nestedLocation)
            Keywords.world -> readWorld(nestedDocumentation, nestedGate, nestedLocation)
            else -> errorWit(nestedLocation, "unexpected identifier: $identifier")
          }
        }

        PackageKind.Nested
      }

      else -> {
        source.readLiteral(';')
        PackageKind.Identifier
      }
    }

    return Package(
      documentation = documentation,
      gate = gate,
      location = location,
      name = name,
      declarations = declarations,
    ) to packageKind
  }

  private enum class PackageKind {
    /** The package of the top of a .wit document. This must not have '{' curly braces '}'. */
    Identifier,

    /** A nested package. This must have '{' curly braces '}'. */
    Nested
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
    val declarations = readInterfaceItems()
    return Interface(
      documentation = documentation,
      gate = gate,
      location = location,
      name = name,
      declarations = declarations,
    )
  }

  private fun readInterfaceItems(): List<Declaration> {
    source.skipWhitespace()
    source.readLiteral('{')

    val result = mutableListOf<Declaration>()
    while (true) {
      source.skipWhitespace()
      if (source.tryReadLiteral('}')) break

      result += readInterfaceItem()
    }
    return result
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
      else -> readFuncItem(documentation, gate, location, identifier)
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

    val fields = source.readCommaSeparatedList {
      val fieldGate = readGateOrNull()
      val fieldDocumentation = source.takeDocumentation()
      val fieldLocation = source.location
      val fieldName = source.readIdentifier()

      source.skipWhitespace()
      source.readLiteral(':')

      source.skipWhitespace()
      val fieldType = source.readTypeName()

      Field(
        documentation = fieldDocumentation,
        gate = fieldGate,
        location = fieldLocation,
        name = fieldName,
        type = fieldType,
      )
    }

    return Record(
      documentation = documentation,
      gate = gate,
      location = location,
      name = name,
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

    val cases = source.readCommaSeparatedList {
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

      Case(
        documentation = caseDocumentation,
        gate = caseGate,
        location = caseLocation,
        name = caseName,
        type = typeName,
      )
    }

    return Variant(
      documentation = documentation,
      gate = gate,
      location = location,
      name = name,
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
    val name = source.readIdentifier()
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
            declarations += readFuncItem(
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
      functions = declarations,
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

    val flags = source.readCommaSeparatedList {
      val flagGate = readGateOrNull()
      val flagDocumentation = source.takeDocumentation()
      val flagLocation = source.location
      val flagName = source.readIdentifier()

      Flag(
        documentation = flagDocumentation,
        gate = flagGate,
        location = flagLocation,
        name = flagName,
      )
    }

    return Flags(
      documentation = documentation,
      gate = gate,
      location = location,
      name = name,
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

    val cases = source.readCommaSeparatedList {
      val caseGate = readGateOrNull()
      val caseDocumentation = source.takeDocumentation()
      val caseLocation = source.location
      val caseName = source.readIdentifier()

      Case(
        documentation = caseDocumentation,
        gate = caseGate,
        location = caseLocation,
        name = caseName,
      )
    }

    return Enum(
      documentation = documentation,
      gate = gate,
      location = location,
      name = name,
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
      name = name,
      target = type,
    )
  }

  /**
   * ```ebnf
   * toplevel-use-item ::= 'use' use-path ('as' id)? ';'
   * ```
   */
  private fun readTopLevelUse(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): TopLevelUse {
    source.skipWhitespace()
    val path = source.readUsePath()

    source.skipWhitespace()
    val alias = when {
      source.tryReadLiteral("as") -> {
        source.skipWhitespace()
        source.readIdentifier()
      }

      else -> null
    }

    source.skipWhitespace()
    source.readLiteral(';')

    return TopLevelUse(
      documentation = documentation,
      gate = gate,
      location = location,
      path = path,
      alias = alias,
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
    val path = source.readUsePath()

    source.skipWhitespace()
    source.readLiteral('.')

    val items = source.readCommaSeparatedList {
      val itemName = source.readIdentifier()

      source.skipWhitespace()
      val alias = when {
        source.tryReadLiteral("as") -> {
          source.skipWhitespace()
          source.readIdentifier()
        }

        else -> null
      }

      Use.Item(
        type = TypeName.Declared(itemName),
        alias = alias,
      )
    }

    source.skipWhitespace()
    source.readLiteral(';')

    return Use(
      documentation = documentation,
      gate = gate,
      location = location,
      path = path,
      items = items,
    )
  }

  /**
   * ```ebnf
   * func-item ::= id ':' func-type ';'
   * ```
   */
  private fun readFuncItem(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
    identifier: Identifier,
  ): Function {
    source.skipWhitespace()
    source.readLiteral(':')
    return readFuncType(documentation, gate, location, identifier)
  }

  /**
   * ```ebnf
   * func-type ::= 'async'? 'func' param-list result-list
   *
   * result-list ::= ϵ
   *               | '->' ty
   * ```
   */
  private fun readFuncType(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
    identifier: Identifier,
  ): Function {
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
    return source.readCommaSeparatedList(minSize = 0, '(', ')') {
      val location = source.location
      val parameterName = source.readIdentifier()

      source.skipWhitespace()
      source.readLiteral(':')

      source.skipWhitespace()
      val parameterType = source.readTypeName()

      Parameter(
        location = location,
        name = parameterName,
        type = parameterType,
      )
    }
  }

  /**
   * ```ebnf
   * world-item ::= gate 'world' id '{' world-items* '}'
   * ```
   */
  internal fun readWorld(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): World {
    source.skipWhitespace()
    val name = source.readIdentifier()

    source.skipWhitespace()
    source.readLiteral('{')

    val declarations = mutableListOf<Declaration>()

    while (true) {
      source.skipWhitespace()
      if (source.tryReadLiteral('}')) break

      declarations += readWorldItem()
    }

    return World(
      documentation = documentation,
      gate = gate,
      location = location,
      name = name,
      declarations = declarations,
    )
  }

  /**
   * ```ebnf
   * world-items ::= gate world-definition
   *
   * world-definition ::= export-item
   *                    | import-item
   *                    | use-item
   *                    | typedef-item
   *                    | include-item
   * ```
   */
  internal fun readWorldItem(): Declaration {
    val gate = readGateOrNull()
    val documentation = source.takeDocumentation()
    val location = source.location

    return when (val identifier = source.readIdentifier()) {
      Keywords.enum -> readEnum(documentation, gate, location)
      Keywords.export -> readExport(documentation, gate, location)
      Keywords.flags -> readFlags(documentation, gate, location)
      Keywords.import -> readImport(documentation, gate, location)
      Keywords.include -> readInclude(documentation, gate, location)
      Keywords.record -> readRecord(documentation, gate, location)
      Keywords.resource -> readResource(documentation, gate, location)
      Keywords.type -> readTypeAlias(documentation, gate, location)
      Keywords.use -> readUse(documentation, gate, location)
      Keywords.variant -> readVariant(documentation, gate, location)
      else -> errorWit(location, "unexpected identifier: $identifier")
    }
  }

  /**
   * ```ebnf
   * import-item ::= 'import' id ':' extern-type
   *               | 'import' use-path ';'
   * ```
   */
  private fun readImport(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): Import {
    return source.select(
      {
        source.skipWhitespace()
        val identifier = source.readIdentifier()
        source.skipWhitespace()
        source.readLiteral(':')
        val value = readExternalType(documentation, gate, location, identifier)
        when {
          // Omit documentation on the import if it's been applied to the imported value.
          value is Declaration -> {
            Import(
              location = location,
              value = value,
            )
          }

          else -> {
            Import(
              documentation = documentation,
              gate = gate,
              location = location,
              value = value,
            )
          }
        }
      },
      {
        source.skipWhitespace()
        val path = source.readUsePath()
        source.skipWhitespace()
        source.readLiteral(';')
        Import(
          documentation = documentation,
          gate = gate,
          location = location,
          value = ExternalUsePath(path = path),
        )
      },
    )
  }

  /**
   * ```ebnf
   * export-item ::= 'export' id ':' extern-type
   *               | 'export' use-path ';'
   * ```
   */
  private fun readExport(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): Export {
    return source.select(
      {
        source.skipWhitespace()
        val identifier = source.readIdentifier()
        source.skipWhitespace()
        source.readLiteral(':')
        val value = readExternalType(documentation, gate, location, identifier)
        when {
          // Omit documentation on the import if it's been applied to the exported value.
          value is Declaration -> {
            Export(
              location = location,
              value = value,
            )
          }

          else -> {
            Export(
              documentation = documentation,
              gate = gate,
              location = location,
              value = value,
            )
          }
        }
      },
      {
        source.skipWhitespace()
        val path = source.readUsePath()
        source.skipWhitespace()
        source.readLiteral(';')
        Export(
          documentation = documentation,
          gate = gate,
          location = location,
          value = ExternalUsePath(path = path),
        )
      },
    )
  }

  /**
   * ```ebnf
   * extern-type ::= func-type ';'
   *               | 'interface' '{' interface-items* '}'
   *               | use-path ';'
   * ```
   */
  private fun readExternalType(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
    identifier: Identifier,
  ): ExternalType {
    return source.select(
      {
        readFuncType(documentation, gate, location, identifier)
      },
      {
        source.skipWhitespace()
        source.readLiteral("interface")
        val declarations = readInterfaceItems()
        Interface(
          documentation = documentation,
          gate = gate,
          location = location,
          name = identifier,
          declarations = declarations,
        )
      },
      {
        source.skipWhitespace()
        val path = source.readUsePath()
        source.readLiteral(';')
        ExternalUsePath(
          plainName = identifier,
          path = path,
        )
      },
    )
  }

  /**
   * ```ebnf
   * include-item ::= 'include' use-path ';'
   *                | 'include' use-path 'with' '{' include-names-list '}'
   *
   * include-names-list ::= include-names-item
   *                      | include-names-list ',' include-names-item
   *
   * include-names-item ::= id 'as' id
   * ```
   */
  private fun readInclude(
    documentation: Documentation?,
    gate: Gate?,
    location: Location,
  ): Include {
    source.skipWhitespace()
    val path = source.readUsePath()

    source.skipWhitespace()
    val items = when {
      source.tryReadLiteral("with") -> {
        source.readCommaSeparatedList {
          source.skipWhitespace()
          val name = TypeName.Declared(source.readIdentifier())

          source.skipWhitespace()
          source.readLiteral("as")

          source.skipWhitespace()
          val alias = source.readIdentifier()

          Include.Item(
            type = name,
            alias = alias,
          )
        }
      }

      else -> listOf()
    }

    source.skipWhitespace()
    source.readLiteral(';')

    return Include(
      documentation = documentation,
      gate = gate,
      location = location,
      path = path,
      items = items,
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

internal object Keywords {
  val `interface` = Identifier("interface")
  val `package` = Identifier("package")
  val async = Identifier("async")
  val bool = Identifier("bool")
  val borrow = Identifier("borrow")
  val char = Identifier("char")
  val constructor = Identifier("constructor")
  val deprecated = Identifier("deprecated")
  val enum = Identifier("enum")
  val export = Identifier("export")
  val f32 = Identifier("f32")
  val f64 = Identifier("f64")
  val feature = Identifier("feature")
  val flags = Identifier("flags")
  val func = Identifier("func")
  val future = Identifier("future")
  val import = Identifier("import")
  val include = Identifier("include")
  val list = Identifier("list")
  val map = Identifier("map")
  val option = Identifier("option")
  val record = Identifier("record")
  val resource = Identifier("resource")
  val result = Identifier("result")
  val s16 = Identifier("s16")
  val s32 = Identifier("s32")
  val s64 = Identifier("s64")
  val s8 = Identifier("s8")
  val since = Identifier("since")
  val static = Identifier("static")
  val stream = Identifier("stream")
  val string = Identifier("string")
  val tuple = Identifier("tuple")
  val type = Identifier("type")
  val u16 = Identifier("u16")
  val u32 = Identifier("u32")
  val u64 = Identifier("u64")
  val u8 = Identifier("u8")
  val unstable = Identifier("unstable")
  val use = Identifier("use")
  val variant = Identifier("variant")
  val version = Identifier("version")
  val world = Identifier("world")
}
