@file:OptIn(WitCoreInternalApi::class)

package com.wasmo.support.wit

fun String.toIdentifier(): Identifier {
  val reader = WitStructureReader(this)
  val result = reader.readIdentifier()
  check(reader.exhausted)
  return result
}

fun String.toPackageName(): PackageName {
  val reader = WitStructureReader(this)
  val result = reader.readPackageName()
  check(reader.exhausted)
  return result
}

fun String.toTypeName(): TypeName {
  val reader = WitStructureReader(this)
  val result = reader.readTypeName()
  check(reader.exhausted)
  return result
}

fun String.toUsePath(): UsePath {
  val reader = WitStructureReader(this)
  val result = reader.readUsePath()
  check(reader.exhausted)
  return result
}

fun String.toSemVer(): SemVer {
  val reader = WitStructureReader(this)
  val result = reader.readSemVer()
  check(reader.exhausted)
  return result
}
