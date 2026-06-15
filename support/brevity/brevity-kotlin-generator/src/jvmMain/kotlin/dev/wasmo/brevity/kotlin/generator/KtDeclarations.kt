package dev.wasmo.brevity.kotlin.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import dev.wasmo.brevity.FunctionName

sealed interface KtDeclaration {
  val documentation: String?
}

data class KtWitPackage(
  val packageName: String,
  val items: List<Item>,
) {
  sealed interface Item
}

data class KtExternalApi(
  override val documentation: String?,
  val name: String,
  val type: TypeName,
) : KtDeclaration, KtWorld.Api

data class KtInterface(
  override val documentation: String?,
  val type: ClassName,
  val instanceName: String,
  val items: List<Item>,
) : KtDeclaration, KtWorld.Api, KtWitPackage.Item {
  sealed interface Item : KtDeclaration
}

data class KtWorld(
  override val documentation: String?,
  val type: ClassName,
  val items: List<Item>,
  val host: Host,
  val guest: Guest,
) : KtDeclaration, KtWitPackage.Item {
  data class Host(
    val name: String,
    val type: ClassName,
    val apis: List<Api>,
  )

  data class Guest(
    val name: String,
    val type: ClassName,
    val apis: List<Api>,
  )

  sealed interface Item : KtDeclaration
  sealed interface Api : KtDeclaration
}

data class KtEnum(
  override val documentation: String?,
  val type: ClassName,
  val cases: List<Case>,
) : KtDeclaration, KtInterface.Item, KtWorld.Item {
  data class Case(
    override val documentation: String?,
    val name: String,
  ) : KtDeclaration
}

data class KtRecord(
  override val documentation: String?,
  val type: ClassName,
  val fields: List<Field>,
) : KtDeclaration, KtInterface.Item, KtWorld.Item {
  data class Field(
    override val documentation: String?,
    val name: String,
    val type: TypeName,
  ) : KtDeclaration
}

data class KtResource(
  override val documentation: String?,
  val type: ClassName,
  val functions: List<KtFunction>,
) : KtDeclaration, KtInterface.Item, KtWorld.Item

data class KtTypeAlias(
  override val documentation: String?,
  val type: ClassName,
  val target: TypeName,
) : KtDeclaration, KtInterface.Item, KtWorld.Item

data class KtVariant(
  override val documentation: String?,
  val type: ClassName,
  val cases: List<Case>,
) : KtDeclaration, KtInterface.Item, KtWorld.Item {
  data class Case(
    override val documentation: String?,
    val name: String,
    val type: TypeName?,
  ) : KtDeclaration
}

data class KtFlags(
  override val documentation: String?,
  val type: ClassName,
  val flags: List<Flag>,
) : KtDeclaration, KtInterface.Item, KtWorld.Item {
  data class Flag(
    override val documentation: String?,
    val name: String,
  ) : KtDeclaration
}

data class KtFunction(
  override val documentation: String?,
  val ktName: String,
  val name: FunctionName,
  val parameters: List<Parameter>,
  val returnType: TypeName?,
) : KtDeclaration, KtInterface.Item, KtWorld.Api {
  data class Parameter(
    override val documentation: String?,
    val name: String,
    val type: TypeName,
  ) : KtDeclaration
}
