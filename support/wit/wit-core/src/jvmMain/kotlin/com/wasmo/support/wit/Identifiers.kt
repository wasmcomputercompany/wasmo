package com.wasmo.support.wit

/**
 * This is a package name plus an interface name, or just an interface name. The encoded form always
 * puts the version at the end of the entire string.
 */
data class UsePath(
  val packageName: PackageName?,
  val name: Identifier,
) {
  companion object {
    operator fun invoke(
      namespaces: List<Identifier> = listOf(),
      packageNames: List<Identifier> = listOf(),
      name: Identifier,
      version: SemVer? = null,
    ) = UsePath(
      PackageName(
        namespaces = namespaces,
        names = packageNames,
        version = version,
      ),
      name,
    )

    operator fun invoke(name: Identifier) = UsePath(null, name)
  }

  override fun toString() = buildString {
    if (packageName != null) {
      for (namespace in packageName.namespaces) {
        append(namespace)
        append(':')
      }
      for (packageName in packageName.names) {
        append(packageName)
        append('/')
      }
    }
    append(name)
    if (packageName?.version != null) {
      append('@')
      append(packageName.version)
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
) : Comparable<PackageName> {
  init {
    check(namespaces.isNotEmpty() && names.isNotEmpty())
  }

  override fun compareTo(other: PackageName) = toString().compareTo(other.toString())

  override fun toString() = buildString {
    for (name in namespaces) {
      append(name)
      append(':')
    }
    for ((index, name) in names.withIndex()) {
      if (index > 0) append('/')
      append(name)
    }
    if (version != null) {
      append("@")
      append(version.version)
    }
  }
}

@JvmInline
value class SemVer(
  val version: String,
) {
  override fun toString() = version
}
