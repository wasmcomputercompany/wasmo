package wasmo.sql

import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.WasmoPostgresqlConfig
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

    val provisioningAddress = WasmoPostgresqlConfig(
      hostname = System.getenv("POSTGRESQL_HOSTNAME") ?: "localhost",
      ssl = false,
      adminUser = "postgres",
      adminPassword = "password",
      adminDatabaseName = "postgres",
      osUser = databaseName,
      osPassword = "password",
      osDatabaseName = databaseName,
    )

    val postgresqlClientFactory = PostgresqlClient.Factory()
    postgresqlClientFactory.prepareTestDatabase(provisioningAddress)

    val client = postgresqlClientFactory
      .connect(provisioningAddress.os)

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
  address: WasmoPostgresqlConfig,
) {
  // Create the test database if it doesn't exist already. This authenticates as superuser.
  connect(address.admin).use { client ->
    try {
      client.withConnection {
        execute("CREATE DATABASE ${address.osDatabaseName} WITH ENCODING = 'UTF8'")
        execute("CREATE USER ${address.osUser} WITH PASSWORD '${address.osPassword}' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT")
        execute("GRANT ALL PRIVILEGES ON DATABASE ${address.osDatabaseName} TO ${address.osUser}")
      }
    } catch (_: PgException) {
      // Assume this database exists.
    }
  }

  // Drop the database.
  connect(address.adminToOsDatabase).use { postgresqlClient ->
    postgresqlClient.withConnection {
      execute("DROP SCHEMA IF EXISTS public CASCADE")
      execute("CREATE SCHEMA public")
      execute("GRANT ALL ON SCHEMA public TO ${address.osUser}")
    }
  }
}
