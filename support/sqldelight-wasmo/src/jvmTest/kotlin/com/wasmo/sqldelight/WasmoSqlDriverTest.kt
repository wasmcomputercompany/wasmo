@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.sqldelight

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.containsExactly
import com.wasmo.sql.testing.AllTypes
import com.wasmo.sql.testing.PostgresqlTester
import com.wasmo.sql.testing.createTableAllTypes
import com.wasmo.sql.testing.insertIntoAllTypes
import com.wasmo.sql.testing.selectFromAllTypes
import kotlin.test.Test
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import wasmo.json.JsonLiteral

class WasmoSqlDriverTest {
  @InterceptTest
  private val tester = PostgresqlTester()

  @Test
  fun `read and write all types`() = runTest {
    val value = AllTypes(
      valueBool = true,
      valueS32 = 1234567890,
      valueS64 = 1234567890123456789L,
      valueF32 = 3.4028235E38f,
      valueF64 = 1.7976931348623157E308,
      valueInstant = Instant.parse("2026-02-01T14:00:00Z"),
      valueString = "Wasmo!",
      valueBytes = "some bytes".encodeUtf8(),
      valueUuid = Uuid.parse("00000000-0000-4000-8000-000000000000"),
      valueJson = JsonLiteral("""{"a":1,"b":2}"""), // Must be in canonical form.
    )
    val database = tester.sqlService.getOrCreate()
    database.newConnection().use { connection ->
      connection.createTableAllTypes()
      connection.insertIntoAllTypes(value)
      assertThat(connection.selectFromAllTypes()).containsExactly(value)
    }
  }
}
