package com.wasmo.support.wit

sealed interface Declaration {
  val documentation: Documentation?
  val gate: Gate?
  val offset: Offset
}

sealed interface TypeDeclaration : Declaration {
  val name: Identifier
}

@JvmInline
value class Documentation(
  val content: String,
)

/**
 * Gates are like annotations syntactically.
 *
 * ```wit
 * @unstable(feature = fancier-foo)
 * @since(version = 0.2.0)
 * @deprecated(version = 0.2.2)
 * interface foo {}
 * ```
 */
data class Gate(
  val unstable: Identifier? = null,
  val since: SemVer? = null,
  val deprecated: SemVer? = null,
) {
  init {
    require(unstable != null || since != null || deprecated != null)
  }

  override fun toString() = buildString {
    if (unstable != null) {
      append("@unstable(feature = ")
      append(unstable)
      append(")")
    }
    if (since != null) {
      if (isNotEmpty()) append(" ")
      append("@since(version = ")
      append(since)
      append(")")
    }
    if (deprecated != null) {
      if (isNotEmpty()) append(" ")
      append("@deprecated(version = ")
      append(deprecated)
      append(")")
    }
  }
}

data class Offset(
  val line: Int,
  val column: Int,
) {
  override fun toString() = "$line:$column"
}

/**
 * An inline package.
 *
 * ```wit
 * package local:a {
 *     interface foo {}
 * }
 * ```
 */
data class Package(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: PackageName? = null,
  val declarations: List<Declaration>,
) : Declaration

/**
 * Declarations may be:
 *
 *  * [Use]
 *  * Type Declarations
 *    * [Resource]
 *    * [Record]
 *    * [Variant]
 *    * [Enum]
 *    * [Flags]
 *    * [TypeAlias]
 *  * [Function]
 */
data class Interface(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val declarations: List<Declaration>,
) : Declaration, World.Api

data class World(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val declarations: List<Declaration>,
  val imports: List<Api>,
  val exports: List<Api>,
) : Declaration {
  sealed interface Api : Declaration
}

data class Resource(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val functions: List<Function>,
) : TypeDeclaration

data class Record(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val fields: List<Field>,
) : TypeDeclaration

data class Field(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val type: TypeName,
) : Declaration

data class Function(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val async: Boolean = false,
  val static: Boolean = false,
  val constructor: Boolean = false,
  val name: Identifier,
  val parameters: List<Parameter>,
  val returnType: TypeName? = null,
) : Declaration, World.Api

data class Variant(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val cases: List<Case>,
) : TypeDeclaration

data class Enum(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val cases: List<Case>,
) : TypeDeclaration

data class Case(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val type: TypeName? = null,
) : Declaration

data class Parameter(
  val documentation: Documentation? = null,
  val offset: Offset,
  val name: Identifier,
  val type: TypeName,
)

data class Flags(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val flags: List<Flag>,
) : TypeDeclaration

data class Flag(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
) : Declaration

data class TypeAlias(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val target: TypeName,
) : TypeDeclaration

data class TopLevelUse(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val path: UsePath,
  val alias: Identifier? = null,
) : Declaration

/**
 * Examples:
 *
 * ```wit
 * use wasi:http/types@1.0.0.{request, response};
 * use types.{request, response};
 * use types.{errno};
 * use types.{errno as my-errno};
 * ```
 */
data class Use(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val path: UsePath,
  val items: List<Item>,
) : Declaration {
  data class Item(
    override val documentation: Documentation? = null,
    override val gate: Gate? = null,
    override val offset: Offset,
    val type: TypeName.Declared,
    val alias: Identifier? = null,
  ) : Declaration
}

data class ExternalUsePath(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val plainName: Identifier? = null,
  val path: UsePath,
) : World.Api

/**
 * Examples:
 *
 * ```wit
 * include wasi:io/my-world-1 with { a as a1, b as b1 };
 * include my-world-2;
 * ```
 */
data class Include(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val path: UsePath,
  val items: List<Item>,
) : Declaration {
  data class Item(
    override val documentation: Documentation? = null,
    override val gate: Gate? = null,
    override val offset: Offset,
    val type: TypeName.Declared,
    val alias: Identifier,
  ) : Declaration
}
