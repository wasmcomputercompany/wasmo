@file:OptIn(WitCoreInternalApi::class)

package dev.wasmo.brevity

import dev.wasmo.brevity.io.WitSyntaxReader

fun String.toIdentifier(): Identifier {
  val reader = WitSyntaxReader(this)
  val result = reader.readIdentifier()
  check(reader.exhausted)
  return result
}

fun String.toPackageName(): PackageName {
  val reader = WitSyntaxReader(this)
  val result = reader.readPackageName()
  check(reader.exhausted)
  return result
}

fun String.toSemVer(): SemVer {
  val reader = WitSyntaxReader(this)
  val result = reader.readSemVer()
  check(reader.exhausted)
  return result
}

fun Gate(
  unstable: String? = null,
  since: String? = null,
  deprecated: String? = null,
) = Gate(
  unstable = unstable?.let { Identifier(it) },
  since = since?.toSemVer(),
  deprecated = deprecated?.toSemVer(),
)
