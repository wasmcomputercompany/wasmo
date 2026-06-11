package com.wasmo.support.wit

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
