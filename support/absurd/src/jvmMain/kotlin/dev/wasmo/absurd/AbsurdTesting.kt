package dev.wasmo.absurd

import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgException
import io.vertx.sqlclient.SqlClient
import okio.FileSystem
import okio.Path.Companion.toPath


/** Creates the test database (if it doesn't exist), and clears it. */
suspend fun prepareTestDatabase(
  superuser: PgConnectOptions,
  test: PgConnectOptions,
) {
  // Create the test database if it doesn't exist already. This authenticates as superuser.
  PostgresqlClient(superuser).use { client ->
    try {
      client.withConnection {
        execute("CREATE DATABASE ${test.database} WITH ENCODING = 'UTF8'")
        execute("CREATE USER ${test.user} WITH PASSWORD '${test.password}' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT")
        execute("GRANT ALL PRIVILEGES ON DATABASE ${test.database} TO ${test.user}")
        execute("ALTER DATABASE ${test.database} OWNER TO ${test.user}")
      }
    } catch (_: PgException) {
      // Assume this database exists.
    }
  }

  // Drop the test database's schema. This authenticates as superuser to the newly-created database.
  val userDatabaseAsSuperuser = PgConnectOptions(superuser)
  userDatabaseAsSuperuser.database = test.database
  PostgresqlClient(userDatabaseAsSuperuser).use { client ->
    client.withConnection {
      execute("DROP SCHEMA IF EXISTS public CASCADE")
      execute("CREATE SCHEMA public")
      execute("GRANT ALL ON SCHEMA public TO ${test.user}")
    }
  }
}

/** This extremely destructive operation drops all absurd data. It may be useful for tests. */
suspend fun SqlClient.dangerouslyClearAbsurdSchema() {
  execute("DROP SCHEMA IF EXISTS absurd CASCADE")
  execute("CREATE SCHEMA absurd")
  execute("GRANT ALL ON SCHEMA absurd TO postgres")
  execute("GRANT ALL ON SCHEMA absurd TO public")
}

suspend fun SqlClient.initAbsurdSchema() {
  val absurdDotSql = FileSystem.RESOURCES.read("/absurd/sql/absurd.sql".toPath()) {
    readUtf8()
  }
  execute(absurdDotSql)
}
