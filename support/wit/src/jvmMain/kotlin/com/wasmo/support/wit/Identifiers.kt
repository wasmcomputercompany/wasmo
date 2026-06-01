package com.wasmo.support.wit

data class QualifiedIdentifier(
  val packageName: PackageName? = null,
  val identifier: Identifier,
)

@JvmInline
value class Identifier(
  val name: String,
)

data class PackageName(
  val namespace: Identifier,
  val name: Identifier,
  val version: SemVer? = null,
) {
  companion object {
    operator fun invoke(namespace: String, name: String, version: String) = PackageName(
      namespace = Identifier(namespace),
      name = Identifier(name),
      version = SemVer(version),
    )
  }
}

@JvmInline
value class SemVer(
  val version: String,
)

data class TypeName(
  val name: Identifier,
  val parameters: List<Identifier> = listOf(),
) {
  companion object {
    operator fun invoke(
      name: String,
      parameters: List<Identifier> = listOf(),
    ) = TypeName(
      Identifier(name),
      parameters,
    )
  }
}
