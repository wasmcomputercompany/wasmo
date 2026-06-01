package com.wasmo.support.wit

sealed interface Declaration {
  val documentation: Documentation?
  val annotations: Annotations?
  val location: Location
}

@JvmInline
value class Documentation(
  val content: String,
)

data class Annotations(
  val since: SemVer? = null,
  val deprecated: SemVer? = null,
  val unstable: Identifier? = null,
)

data class Location(
  val line: Int,
  val column: Int,
)

/**
 * Declarations may be [Use], [Interface], [World].
 */
data class WitFile(
  val packageName: PackageName? = null,
  val declarations: List<Declaration>,
)

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
  override val documentation: Documentation?,
  override val annotations: Annotations?,
  override val location: Location,
  val packageName: PackageName? = null,
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
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: TypeName,
  val declarations: List<Declaration>,
) : Declaration

data class World(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: TypeName,
  val declarations: List<Declaration>,
) : Declaration

data class Resource(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: TypeName,
  val declarations: List<Declaration>,
) : Declaration

data class Record(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: TypeName,
  val fields: List<Field>,
) : Declaration

data class Field(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: Identifier,
  val typeName: TypeName,
) : Declaration

data class Function(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val static: Boolean = false,
  val constructor: Boolean = false,
  val name: Identifier,
  val parameters: List<Parameter>,
  val returnType: TypeName? = null,
) : Declaration

data class Variant(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: TypeName,
  val cases: List<Case>,
) : Declaration

data class Enum(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: TypeName,
  val cases: List<Case>,
) : Declaration

data class Case(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: Identifier,
  val typeName: TypeName? = null,
) : Declaration

data class Parameter(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: Identifier,
  val typeName: TypeName? = null,
) : Declaration

data class Flags(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: TypeName,
  val flags: List<Flag>,
) : Declaration

data class Flag(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: Identifier,
) : Declaration

data class TypeAlias(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val name: TypeName,
  val target: TypeName,
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
  override val annotations: Annotations? = null,
  override val location: Location,
  val references: List<Reference>,
) : Declaration {
  data class Reference(
    val identifier: QualifiedIdentifier,
    val `as`: Identifier? = null,
  )
}

data class Import(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val value: Either<QualifiedIdentifier, Declaration>,
) : Declaration

data class Export(
  override val documentation: Documentation? = null,
  override val annotations: Annotations? = null,
  override val location: Location,
  val value: Either<QualifiedIdentifier, Declaration>,
) : Declaration
