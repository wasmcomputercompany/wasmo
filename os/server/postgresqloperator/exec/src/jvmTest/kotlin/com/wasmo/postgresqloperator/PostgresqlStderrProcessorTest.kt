package com.wasmo.postgresqloperator

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.testing.events.TestEventListener
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.Buffer

internal class PostgresqlStderrProcessorTest {
  private val eventListener = TestEventListener()
  private val processor = PostgresqlStderrProcessor(eventListener)

  @Test
  fun happyPath() = runTest {
    processor.processAll(
      Buffer().writeUtf8(
        """
        |> @ 00000 LOG:  starting PostgreSQL 18.4 on aarch64-alpine-linux-musl, compiled by cc (Alpine 15.2.0) 15.2.0, 64-bit
        |> @ 00000 LOG:  listening on IPv4 address "0.0.0.0", port 54401
        |> postgres@postgres 00000 LOG:  statement: CREATE USER wasmo_homelab WITH PASSWORD 'password' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT
        |> wasmo_homelab@wasmo_homelab 57P03 FATAL:  the database system is starting up
        |> postgres@postgres 00000 LOG:  statement: GRANT ALL PRIVILEGES ON DATABASE wasmo_homelab TO wasmo_homelab
        |> wasmo_homelab@wasmo_homelab 00000 LOG:  statement:
        |        CREATE TABLE IF NOT EXISTS DatabaseSchemaVersion (
        |          id INTEGER NOT NULL PRIMARY KEY DEFAULT 1,
        |          version INTEGER NOT NULL
        |        )
        |
        |> wasmo_homelab@wasmo_homelab 00000 LOG:  statement: ALTER TABLE DatabaseSchemaVersion DROP CONSTRAINT IF EXISTS only_one_row
        |
        """.trimMargin(),
      ),
    )

    assertThat(eventListener.receive<PostgresqlLogEvent>()).isEqualTo(
      PostgresqlLogEvent(
        message = "starting PostgreSQL 18.4 on aarch64-alpine-linux-musl, compiled by cc (Alpine 15.2.0) 15.2.0, 64-bit",
      ),
    )
    assertThat(eventListener.receive<PostgresqlLogEvent>()).isEqualTo(
      PostgresqlLogEvent(
        message = "listening on IPv4 address \"0.0.0.0\", port 54401",
      ),
    )
    assertThat(eventListener.receive<PostgresqlLogEvent>()).isEqualTo(
      PostgresqlLogEvent(
        user = "postgres",
        database = "postgres",
        message = "statement: CREATE USER wasmo_homelab WITH PASSWORD 'password' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT",
      ),
    )
    assertThat(eventListener.receive<PostgresqlLogEvent>()).isEqualTo(
      PostgresqlLogEvent(
        user = "wasmo_homelab",
        database = "wasmo_homelab",
        sqlStateErrorCode = "57P03",
        severity = "FATAL",
        message = "the database system is starting up",
      ),
    )
    assertThat(eventListener.receive<PostgresqlLogEvent>()).isEqualTo(
      PostgresqlLogEvent(
        user = "postgres",
        database = "postgres",
        message = "statement: GRANT ALL PRIVILEGES ON DATABASE wasmo_homelab TO wasmo_homelab",
      ),
    )
    assertThat(eventListener.receive<PostgresqlLogEvent>()).isEqualTo(
      PostgresqlLogEvent(
        user = "wasmo_homelab",
        database = "wasmo_homelab",
        message = """
          |statement:
          |        CREATE TABLE IF NOT EXISTS DatabaseSchemaVersion (
          |          id INTEGER NOT NULL PRIMARY KEY DEFAULT 1,
          |          version INTEGER NOT NULL
          |        )
          |
          """.trimMargin(),
      ),
    )
    assertThat(eventListener.receive<PostgresqlLogEvent>()).isEqualTo(
      PostgresqlLogEvent(
        user = "wasmo_homelab",
        database = "wasmo_homelab",
        message = "statement: ALTER TABLE DatabaseSchemaVersion DROP CONSTRAINT IF EXISTS only_one_row",
      ),
    )
  }
}
