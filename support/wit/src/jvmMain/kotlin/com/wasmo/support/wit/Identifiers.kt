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
  val namespaces: List<Identifier>,
  val names: List<Identifier>,
  val version: SemVer? = null,
) {
  init {
    check(namespaces.isNotEmpty() && names.isNotEmpty())
  }

  override fun toString() = buildString {
    for (name in namespaces) {
      append(name)
      append(':')
    }
    for ((index, name) in names.withIndex()) {
      if (index > 0) append('/')
      append(name)
    }
  }

  companion object {
    operator fun invoke(namespace: String, name: String, version: String? = null) = PackageName(
      namespaces = listOf(Identifier(namespace)),
      names = listOf(Identifier(name)),
      version = version?.let { SemVer(it) },
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
