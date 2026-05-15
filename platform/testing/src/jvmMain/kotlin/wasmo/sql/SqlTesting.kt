package wasmo.sql

import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.asSqlDatabase
import com.wasmo.sql.execute
import io.vertx.pgclient.PgException

class FakeSqlService(
  val databaseName: String,
) : SqlService {
  /** Cached instance so [getOrCreate] is idempotent. */
  private var sqlDatabase: SqlDatabase? = null

  override suspend fun getOrCreate(name: String): SqlDatabase {
    require(name.isEmpty()) { "unexpected database name: $name" }

    val existing = sqlDatabase
    if (existing != null) return existing

    val adminPostgresqlAddress = PostgresqlAddress(
      databaseName = "postgres",
      user = "postgres",
      password = "password",
      hostname = System.getenv("POSTGRESQL_HOSTNAME") ?: "localhost",
      ssl = false,
    )

    val postgresqlAddress = adminPostgresqlAddress.copy(
      databaseName = databaseName,
      user = databaseName,
    )

    val postgresqlClientFactory = PostgresqlClient.Factory()
    postgresqlClientFactory.prepareTestDatabase(adminPostgresqlAddress, postgresqlAddress)

    val client = postgresqlClientFactory
      .connect(postgresqlAddress)

    val result = client.asSqlDatabase()
    sqlDatabase = result
    return result
  }

  override fun close() {
    sqlDatabase?.close()
  }
}

/** Creates the test database (if it doesn't exist), and clears it. */
suspend fun PostgresqlClient.Factory.prepareTestDatabase(
  superuser: PostgresqlAddress,
  test: PostgresqlAddress,
) {
  // Create the test database if it doesn't exist already. This authenticates as superuser.
  connect(superuser).use { postgresqlClient ->
    try {
      postgresqlClient.withConnection {
        execute("CREATE DATABASE ${test.databaseName} WITH ENCODING = 'UTF8'")
        execute("CREATE USER ${test.user} WITH PASSWORD '${test.password}' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT")
        execute("GRANT ALL PRIVILEGES ON DATABASE ${test.databaseName} TO ${test.user}")
      }
    } catch (_: PgException) {
      // Assume this database exists.
    }
  }

  // Drop the database. This authenticates as superuser to the newly-created database.
  connect(superuser.copy(databaseName = test.databaseName)).use { postgresqlClient ->
    postgresqlClient.withConnection {
      execute("DROP SCHEMA IF EXISTS public CASCADE")
      execute("CREATE SCHEMA public")
      execute("GRANT ALL ON SCHEMA public TO ${test.user}")
    }
  }
}
