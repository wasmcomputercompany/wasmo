@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.sql.vertx

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import wasmo.json.JsonLiteral

class PostgresqlSqlServiceTest {
  @InterceptTest
  private val tester = VertxPostgresTester()

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
      valueJson = JsonLiteral("""{"a": 1, "b": 2}"""), // Must be in canonical form.
    )
    val database = tester.sqlService.getOrCreate()
    database.newConnection().use { connection ->
      connection.createTableAllTypes()
      connection.insertIntoAllTypes(value)
      assertThat(connection.selectFromAllTypes()).containsExactly(value)
    }
  }

  @Test
  fun `read and write all types as null`() = runTest {
    val value = AllTypes()
    val database = tester.sqlService.getOrCreate()
    database.newConnection().use { connection ->
      connection.createTableAllTypes()
      connection.insertIntoAllTypes(value)
      assertThat(connection.selectFromAllTypes()).containsExactly(value)
    }
  }

  @Test
  fun `transaction commits`() = runTest {
    val database = tester.sqlService.getOrCreate()
    database.newConnection().use { connection ->
      connection.createTableBalances()
      connection.insertBalances(
        Balance("mike", 200_00L),
        Balance("jesse", 100_00L),
      )

      connection.execute("""BEGIN""")
      connection.updateBalances(
        Balance("mike", 220_00L),
        Balance("jesse", 80_00L),
      )
      connection.execute("""COMMIT""")

      assertThat(connection.allBalances()).containsExactly(
        Balance("mike", 220_00L),
        Balance("jesse", 80_00L),
      )
    }
  }

  @Test
  fun `transaction rollback`() = runTest {
    val database = tester.sqlService.getOrCreate()
    database.newConnection().use { connection ->
      connection.createTableBalances()
      connection.insertBalances(
        Balance("mike", 200_00L),
        Balance("jesse", 100_00L),
      )

      connection.execute("""BEGIN""")
      connection.updateBalances(
        Balance("mike", 220_00L),
        Balance("jesse", 80_00L),
      )
      connection.execute("""ROLLBACK""")

      assertThat(connection.allBalances()).containsExactly(
        Balance("mike", 200_00L),
        Balance("jesse", 100_00L),
      )
    }
  }

  @Test
  fun `transaction scoped settings are isolated`() = runTest {
    val database = tester.sqlService.getOrCreate()
    database.newConnection().use { connection ->
      connection.executeQuery(
        "SELECT current_setting('TIMEZONE')",
      ).use { rowIterator ->
        val row = rowIterator.next()!!
        assertThat(row.getString(0)).isEqualTo("Etc/UTC")
      }
    }
    database.newConnection().use { connection ->
      connection.execute("SET TIME ZONE 'America/Toronto'")
    }
    database.newConnection().use { connection ->
      connection.executeQuery(
        "SELECT current_setting('TIMEZONE')",
      ).use { rowIterator ->
        val row = rowIterator.next()!!
        assertThat(row.getString(0)).isEqualTo("Etc/UTC")
      }
    }
  }

  @Test
  fun `dangling commit is rolled back`() = runTest {
    val database = tester.sqlService.getOrCreate()
    database.newConnection().use { connection ->
      connection.createTableBalances()
      connection.insertBalances(
        Balance("mike", 200_00L),
        Balance("jesse", 100_00L),
      )

      connection.execute("""BEGIN""")
      connection.updateBalances(
        Balance("mike", 220_00L),
        Balance("jesse", 80_00L),
      )
    }

    database.newConnection().use { connection ->
      assertThat(connection.allBalances()).containsExactly(
        Balance("mike", 200_00L),
        Balance("jesse", 100_00L),
      )
    }
  }
}
