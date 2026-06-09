@file:OptIn(WitCoreInternalApi::class)

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
      val offset = source.offset

      when (val identifier = source.readIdentifier()) {
        Keywords.`package` -> {
          val (value, kind) = readPackage(documentation, gate, offset)
          when (kind) {
            PackageKind.Identifier -> {
              checkWit(packageIdentifier == null && declarations.isEmpty(), offset) {
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
          declarations += readInterface(documentation, gate, offset)
        }

        Keywords.use -> {
          declarations += readTopLevelUse(documentation, gate, offset)
        }

        Keywords.world -> {
          declarations += readWorld(documentation, gate, offset)
        }

        else -> errorWit(offset, "unexpected identifier: $identifier")
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
    offset: Offset,
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
          val nestedOffset = source.offset

          declarations += when (val identifier = source.readIdentifier()) {
            Keywords.`interface` -> readInterface(nestedDocumentation, nestedGate, nestedOffset)
            Keywords.use -> readTopLevelUse(nestedDocumentation, nestedGate, nestedOffset)
            Keywords.world -> readWorld(nestedDocumentation, nestedGate, nestedOffset)
            else -> errorWit(nestedOffset, "unexpected identifier: $identifier")
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
      offset = offset,
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
    offset: Offset,
  ): Interface {
    source.skipWhitespace()
    val name = source.readIdentifier()
    val declarations = readInterfaceItems()
    return Interface(
      documentation = documentation,
      gate = gate,
      offset = offset,
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
    val offset = source.offset

    return when (val identifier = source.readIdentifier()) {
      Keywords.enum -> readEnum(documentation, gate, offset)
      Keywords.flags -> readFlags(documentation, gate, offset)
      Keywords.record -> readRecord(documentation, gate, offset)
      Keywords.resource -> readResource(documentation, gate, offset)
      Keywords.variant -> readVariant(documentation, gate, offset)
      Keywords.type -> readTypeAlias(documentation, gate, offset)
      Keywords.use -> readUse(documentation, gate, offset)
      else -> readFuncItem(documentation, gate, offset, identifier)
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
    offset: Offset,
  ): Record {
    source.skipWhitespace()
    val name = source.readIdentifier()

    val fields = source.readCommaSeparatedList {
      val fieldGate = readGateOrNull()
      val fieldDocumentation = source.takeDocumentation()
      val fieldOffset = source.offset
      val fieldName = source.readIdentifier()

      source.skipWhitespace()
      source.readLiteral(':')

      source.skipWhitespace()
      val fieldType = source.readTypeName()

      Field(
        documentation = fieldDocumentation,
        gate = fieldGate,
        offset = fieldOffset,
        name = fieldName,
        type = fieldType,
      )
    }

    return Record(
      documentation = documentation,
      gate = gate,
      offset = offset,
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
    offset: Offset,
  ): Variant {
    source.skipWhitespace()
    val name = source.readIdentifier()

    val cases = source.readCommaSeparatedList {
      val caseGate = readGateOrNull()
      val caseDocumentation = source.takeDocumentation()
      val caseOffset = source.offset
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
        offset = caseOffset,
        name = caseName,
        type = typeName,
      )
    }

    return Variant(
      documentation = documentation,
      gate = gate,
      offset = offset,
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
    offset: Offset,
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
        val functionOffset = source.offset

        when (val identifier = source.readIdentifier()) {
          Keywords.constructor -> {
            val parameters = readParameterList()
            source.skipWhitespace()
            source.readLiteral(';')
            declarations += Function(
              documentation = functionDocumentation,
              gate = functionGate,
              offset = functionOffset,
              constructor = true,
              name = identifier,
              parameters = parameters,
            )
          }

          else -> {
            declarations += readFuncItem(
              documentation = functionDocumentation,
              gate = functionGate,
              offset = functionOffset,
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
      offset = offset,
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
    offset: Offset,
  ): Flags {
    source.skipWhitespace()
    val name = source.readIdentifier()

    val flags = source.readCommaSeparatedList {
      val flagGate = readGateOrNull()
      val flagDocumentation = source.takeDocumentation()
      val flagOffset = source.offset
      val flagName = source.readIdentifier()

      Flag(
        documentation = flagDocumentation,
        gate = flagGate,
        offset = flagOffset,
        name = flagName,
      )
    }

    return Flags(
      documentation = documentation,
      gate = gate,
      offset = offset,
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
    offset: Offset,
  ): Enum {
    source.skipWhitespace()
    val name = source.readIdentifier()

    val cases = source.readCommaSeparatedList {
      val caseGate = readGateOrNull()
      val caseDocumentation = source.takeDocumentation()
      val caseOffset = source.offset
      val caseName = source.readIdentifier()

      Case(
        documentation = caseDocumentation,
        gate = caseGate,
        offset = caseOffset,
        name = caseName,
      )
    }

    return Enum(
      documentation = documentation,
      gate = gate,
      offset = offset,
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
    offset: Offset,
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
      offset = offset,
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
    offset: Offset,
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
      offset = offset,
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
    offset: Offset,
  ): Use {
    source.skipWhitespace()
    val path = source.readUsePath()

    source.skipWhitespace()
    source.readLiteral('.')

    val items = source.readCommaSeparatedList {
      val itemGate = readGateOrNull()
      val itemDocumentation = source.takeDocumentation()
      val itemOffset = source.offset
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
        gate = itemGate,
        documentation = itemDocumentation,
        offset = itemOffset,
        type = TypeName.Declared(itemName),
        alias = alias,
      )
    }

    source.skipWhitespace()
    source.readLiteral(';')

    return Use(
      documentation = documentation,
      gate = gate,
      offset = offset,
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
    offset: Offset,
    identifier: Identifier,
  ): Function {
    source.skipWhitespace()
    source.readLiteral(':')
    return readFuncType(documentation, gate, offset, identifier)
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
    offset: Offset,
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
        else -> errorWit(offset, "unexpected identifier: $modifier")
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
      offset = offset,
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
      val documentation = source.takeDocumentation()
      val offset = source.offset
      val parameterName = source.readIdentifier()

      source.skipWhitespace()
      source.readLiteral(':')

      source.skipWhitespace()
      val parameterType = source.readTypeName()

      Parameter(
        documentation = documentation,
        offset = offset,
        name = parameterName,
        type = parameterType,
      )
    }
  }

  /**
   * ```ebnf
   * world-item ::= gate 'world' id '{' world-items* '}'
   *
   * world-items ::= gate world-definition
   *
   * world-definition ::= export-item
   *                    | import-item
   *                    | use-item
   *                    | typedef-item
   *                    | include-item
   * ```
   */
  internal fun readWorld(
    documentation: Documentation?,
    gate: Gate?,
    offset: Offset,
  ): World {
    source.skipWhitespace()
    val name = source.readIdentifier()

    source.skipWhitespace()
    source.readLiteral('{')

    val declarations = mutableListOf<Declaration>()
    val imports = mutableListOf<World.Api>()
    val exports = mutableListOf<World.Api>()

    while (true) {
      source.skipWhitespace()
      if (source.tryReadLiteral('}')) break

      val itemGate = readGateOrNull()
      val itemDocumentation = source.takeDocumentation()
      val itemOffset = source.offset
      when (val identifier = source.readIdentifier()) {
        Keywords.enum -> declarations += readEnum(itemDocumentation, itemGate, itemOffset)
        Keywords.export -> exports += readWorldApi(itemDocumentation, itemGate, itemOffset)
        Keywords.flags -> declarations += readFlags(itemDocumentation, itemGate, itemOffset)
        Keywords.import -> imports += readWorldApi(itemDocumentation, itemGate, itemOffset)
        Keywords.include -> declarations += readInclude(itemDocumentation, itemGate, itemOffset)
        Keywords.record -> declarations += readRecord(itemDocumentation, itemGate, itemOffset)
        Keywords.resource -> declarations += readResource(itemDocumentation, itemGate, itemOffset)
        Keywords.type -> declarations += readTypeAlias(itemDocumentation, itemGate, itemOffset)
        Keywords.use -> declarations += readUse(itemDocumentation, itemGate, itemOffset)
        Keywords.variant -> declarations += readVariant(itemDocumentation, itemGate, itemOffset)
        else -> errorWit(offset, "unexpected identifier: $identifier")
      }
    }

    return World(
      documentation = documentation,
      gate = gate,
      offset = offset,
      name = name,
      declarations = declarations,
      imports = imports,
      exports = exports,
    )
  }

  /**
   * ```ebnf
   * import-item ::= 'import' id ':' extern-type
   *               | 'import' use-path ';'
   * export-item ::= 'export' id ':' extern-type
   *               | 'export' use-path ';'
   * ```
   */
  private fun readWorldApi(
    documentation: Documentation?,
    gate: Gate?,
    offset: Offset,
  ): World.Api {
    return source.select(
      {
        source.skipWhitespace()
        val identifier = source.readIdentifier()
        source.skipWhitespace()
        source.readLiteral(':')
        readExternalType(documentation, gate, offset, identifier)
      },
      {
        source.skipWhitespace()
        val path = source.readUsePath()
        source.skipWhitespace()
        source.readLiteral(';')
        ExternalUsePath(
          documentation = documentation,
          gate = gate,
          offset = offset,
          path = path,
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
    offset: Offset,
    identifier: Identifier,
  ): World.Api {
    return source.select(
      {
        readFuncType(documentation, gate, offset, identifier)
      },
      {
        source.skipWhitespace()
        source.readLiteral("interface")
        val declarations = readInterfaceItems()
        Interface(
          documentation = documentation,
          gate = gate,
          offset = offset,
          name = identifier,
          declarations = declarations,
        )
      },
      {
        source.skipWhitespace()
        val path = source.readUsePath()
        source.readLiteral(';')
        ExternalUsePath(
          documentation = documentation,
          gate = gate,
          offset = offset,
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
    offset: Offset,
  ): Include {
    source.skipWhitespace()
    val path = source.readUsePath()

    source.skipWhitespace()
    val items = when {
      source.tryReadLiteral("with") -> {
        source.readCommaSeparatedList {
          val itemGate = readGateOrNull()
          val itemDocumentation = source.takeDocumentation()
          val itemOffset = source.offset
          val type = TypeName.Declared(source.readIdentifier())

          source.skipWhitespace()
          source.readLiteral("as")

          source.skipWhitespace()
          val alias = source.readIdentifier()

          Include.Item(
            documentation = itemDocumentation,
            gate = itemGate,
            offset = itemOffset,
            type = type,
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
      offset = offset,
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
      val offset = source.offset
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

        else -> errorWit(offset, "unexpected field: $gateItem.$fieldName")
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
