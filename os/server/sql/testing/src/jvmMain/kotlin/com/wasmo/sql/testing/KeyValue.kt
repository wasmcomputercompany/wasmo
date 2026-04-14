package com.wasmo.sql.testing

import wasmo.json.JsonLiteral
import wasmo.sql.SqlConnection

suspend fun SqlConnection.createTableKeyValues() {
  execute(
    """
    CREATE TABLE KeyValues (
      key TEXT,
      value JSONB
    )
    """,
  )
}

suspend fun SqlConnection.insertKeyValue(vararg keyValues: KeyValue) {
  for ((key, value) in keyValues) {
    execute("""INSERT INTO KeyValues (key, value) VALUES ($1, $2)""") {
      bindString(0, key)
      bindJson(1, value)
    }
  }
}

data class KeyValue(
  val key: String,
  val value: JsonLiteral,
)
