package com.wasmo.support.wit.io

import com.wasmo.support.wit.Documentation
import com.wasmo.support.wit.Gate
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.Offset
import com.wasmo.support.wit.PackageName
import okio.Path

/**
 * A collection of `.wit` files from a single file system directory.
 */
data class IoWitPackage(
  val packageDocumentation: Documentation? = null,
  val packageName: PackageName,
  val files: Map<Path, IoWitFile>,
)

data class IoWitFile(
  val packageDocumentation: Documentation? = null,
  val packageName: PackageName? = null,
  val items: List<Item> = listOf(),
) {
  sealed interface Item : IoDeclaration
}

sealed interface IoDeclaration {
  val documentation: Documentation?
  val gate: Gate?
  val offset: Offset
}

sealed interface IoTypeDeclaration : IoDeclaration, IoInterface.Item, IoWorld.Item {
  val name: Identifier
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
data class IoInlinePackage(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: PackageName,
  val declarations: List<IoDeclaration>,
) : IoDeclaration, IoWitFile.Item

/**
 * Declarations may be:
 *
 *  * [IoUse]
 *  * Type Declarations
 *    * [IoResource]
 *    * [IoRecord]
 *    * [IoVariant]
 *    * [IoEnum]
 *    * [IoFlags]
 *    * [IoTypeAlias]
 *  * [IoFunction]
 */
data class IoInterface(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val items: List<Item>,
) : IoDeclaration, IoWorld.Api, IoWitFile.Item {
  sealed interface Item : IoDeclaration
}

data class IoWorld(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val items: List<Item>,
  val imports: List<Api>,
  val exports: List<Api>,
) : IoDeclaration, IoWitFile.Item {
  sealed interface Api : IoDeclaration
  sealed interface Item : IoDeclaration
}

data class IoResource(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val functions: List<IoFunction>,
) : IoTypeDeclaration

data class IoRecord(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val fields: List<IoField>,
) : IoTypeDeclaration

data class IoField(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val type: IoTypeName,
) : IoDeclaration

data class IoFunction(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val async: Boolean = false,
  val static: Boolean = false,
  val constructor: Boolean = false,
  val name: Identifier,
  val parameters: List<IoParameter>,
  val returnType: IoTypeName? = null,
) : IoDeclaration, IoWorld.Api, IoInterface.Item

data class IoVariant(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val cases: List<IoCase>,
) : IoTypeDeclaration

data class IoEnum(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val cases: List<IoCase>,
) : IoTypeDeclaration

data class IoCase(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val type: IoTypeName? = null,
) : IoDeclaration

data class IoParameter(
  val documentation: Documentation? = null,
  val offset: Offset,
  val name: Identifier,
  val type: IoTypeName,
)

data class IoFlags(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val flags: List<IoFlag>,
) : IoTypeDeclaration

data class IoFlag(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
) : IoDeclaration

data class IoTypeAlias(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val target: IoTypeName,
) : IoTypeDeclaration

data class IoTopLevelUse(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val path: UsePath,
  val alias: Identifier? = null,
) : IoDeclaration, IoWitFile.Item

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
data class IoUse(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val path: UsePath,
  val items: List<Item>,
) : IoDeclaration, IoInterface.Item, IoWorld.Item {
  data class Item(
    override val documentation: Documentation? = null,
    override val gate: Gate? = null,
    override val offset: Offset,
    val type: IoTypeName.Declared,
    val alias: Identifier? = null,
  ) : IoDeclaration
}

data class IoExternalUsePath(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val plainName: Identifier? = null,
  val path: UsePath,
) : IoDeclaration, IoWorld.Api

/**
 * Examples:
 *
 * ```wit
 * include wasi:io/my-world-1 with { a as a1, b as b1 };
 * include my-world-2;
 * ```
 */
data class IoInclude(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val path: UsePath,
  val items: List<Item>,
) : IoDeclaration, IoWorld.Item {
  data class Item(
    override val documentation: Documentation? = null,
    override val gate: Gate? = null,
    override val offset: Offset,
    val type: IoTypeName.Declared,
    val alias: Identifier,
  ) : IoDeclaration
}
