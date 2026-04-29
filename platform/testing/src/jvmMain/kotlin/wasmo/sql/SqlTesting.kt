package wasmo.sql

import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.asSqlDatabase
import com.wasmo.sql.execute
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

    val postgresqlAddress = PostgresqlAddress(
      databaseName = databaseName,
      user = "postgres",
      password = "password",
      hostname = "localhost",
      ssl = false,
    )

    val client = PostgresqlClient(postgresqlAddress)
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
