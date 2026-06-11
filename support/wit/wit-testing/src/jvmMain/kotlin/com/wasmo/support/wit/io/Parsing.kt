@file:OptIn(WitCoreInternalApi::class)

package com.wasmo.support.wit.io

import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.SemVer
import com.wasmo.support.wit.WitCoreInternalApi

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

fun String.toTypeName(): IoTypeName {
  val reader = WitSyntaxReader(this)
  val result = reader.readTypeName()
  check(reader.exhausted)
  return result
}

fun String.toUsePath(): UsePath {
  val reader = WitSyntaxReader(this)
  val result = reader.readUsePath()
  check(reader.exhausted)
  return result
}

fun String.toSemVer(): SemVer {
  val reader = WitSyntaxReader(this)
  val result = reader.readSemVer()
  check(reader.exhausted)
  return result
}
