package com.wasmo.db

import com.wasmo.api.WasmoJson
import wasmo.sql.SqlBinder
import wasmo.sql.SqlRow


inline fun <reified T> SqlBinder.bindJson(index: Int, value: T) {
  bindString(index, WasmoJson.encodeToString(value))
}

inline fun <reified T> SqlRow.decodeJson(index: Int): T {
  val string = getString(index)!!
  return WasmoJson.decodeFromString<T>(string)
}
