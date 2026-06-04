package com.wasmo.support.wit

data class UsePath(
  val namespaces: List<Identifier> = listOf(),
  val packageNames: List<Identifier> = listOf(),
  val name: Identifier,
  val version: SemVer? = null,
) {
  override fun toString() = buildString {
    for (namespace in namespaces) {
      append(namespace)
      append(':')
    }
    for (packageName in packageNames) {
      append(packageName)
      append('/')
    }
    append(name)
    if (version != null) {
      append('@')
      append(version)
    }
  }
}

@JvmInline
value class Identifier(
  val name: String,
) {
  override fun toString() = name
}

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
) {
  override fun toString() = version
}
