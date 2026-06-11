@file:OptIn(WitCoreInternalApi::class)

package dev.wasmo.brevity.io

import dev.wasmo.brevity.WitCoreInternalApi

fun String.toIoTypeName(): IoTypeName {
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
