package wasmo.sql

import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.asSqlDatabase
import com.wasmo.sql.execute
import io.vertx.pgclient.PgException
import io.vertx.sqlclient.SqlClient

class FakeSqlService(
  val databaseName: String,
  val clearSchema: Boolean,
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

    PostgresqlClient.Factory().connect(adminPostgresqlAddress).use { client ->
      try {
        client.withConnection {
          execute("CREATE DATABASE $databaseName WITH ENCODING = 'UTF8'")
        }
      } catch (_: PgException) {
        // Already exists?
      }
    }

    val postgresqlAddress = adminPostgresqlAddress.copy(
      databaseName = databaseName,
    )

    val client = PostgresqlClient.Factory()
      .connect(postgresqlAddress)
    if (clearSchema) {
      client.withConnection {
        clearSchema()
      }
    }

    val result = client.asSqlDatabase()
    sqlDatabase = result
    return result
  }

  override fun close() {
    sqlDatabase?.close()
  }
}

suspend fun SqlClient.clearSchema() {
  execute("DROP SCHEMA IF EXISTS public CASCADE")
  execute("CREATE SCHEMA public")
  execute("GRANT ALL ON SCHEMA public TO postgres")
  execute("GRANT ALL ON SCHEMA public TO public")
}
