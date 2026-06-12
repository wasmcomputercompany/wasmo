package dev.wasmo.brevity.ir

import dev.wasmo.brevity.Documentation
import dev.wasmo.brevity.FunctionName
import dev.wasmo.brevity.Gate
import dev.wasmo.brevity.Identifier
import dev.wasmo.brevity.Offset
import dev.wasmo.brevity.PackageName

data class IrWitPackage(
  val packageDocumentation: Documentation? = null,
  val packageName: PackageName,
  val items: List<Item> = listOf(),
) {
  sealed interface Item
}

sealed interface IrDeclaration {
  val documentation: Documentation?
  val gate: Gate?
  val offset: Offset
}

sealed interface IrTypeDeclaration : IrDeclaration, IrInterface.Item, IrWorld.Item {
  val name: Identifier
}

data class IrInterface(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val items: List<Item>,
) : IrDeclaration, IrWorld.Api, IrWitPackage.Item {
  sealed interface Item : IrDeclaration
}

data class IrWorld(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val items: List<Item>,
  val imports: List<Api>,
  val exports: List<Api>,
) : IrDeclaration, IrWitPackage.Item {
  sealed interface Api : IrDeclaration
  sealed interface Item : IrDeclaration
}

data class IrResource(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val functions: List<IrFunction>,
) : IrTypeDeclaration

data class IrRecord(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val fields: List<IrField>,
) : IrTypeDeclaration

data class IrField(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val type: IrTypeName,
) : IrDeclaration

data class IrFunction(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val async: Boolean = false,
  val parameters: List<IrParameter>,
  val returnType: IrTypeName? = null,
  val functionName: FunctionName,
) : IrDeclaration, IrWorld.Api, IrInterface.Item

data class IrVariant(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val cases: List<IrCase>,
) : IrTypeDeclaration

data class IrEnum(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val cases: List<IrCase>,
) : IrTypeDeclaration

data class IrCase(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
  val type: IrTypeName? = null,
) : IrDeclaration

data class IrParameter(
  val documentation: Documentation? = null,
  val offset: Offset,
  val name: Identifier,
  val type: IrTypeName,
)

data class IrFlags(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val flags: List<IrFlag>,
) : IrTypeDeclaration

data class IrFlag(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val name: Identifier,
) : IrDeclaration

data class IrTypeAlias(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  override val name: Identifier,
  val target: IrTypeName,
) : IrTypeDeclaration, IrInterface.Item

data class IrExternalApi(
  override val documentation: Documentation? = null,
  override val gate: Gate? = null,
  override val offset: Offset,
  val plainName: Identifier? = null,
  val path: IrParentName,
) : IrDeclaration, IrWorld.Api
