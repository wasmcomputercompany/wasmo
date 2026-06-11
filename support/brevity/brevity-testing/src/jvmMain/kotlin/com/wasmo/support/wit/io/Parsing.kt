@file:OptIn(WitCoreInternalApi::class)

package com.wasmo.support.wit.io

import com.wasmo.support.wit.WitCoreInternalApi

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
