package wasmo.sql

import com.wasmo.identifiers.Secret
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.RealOsDatabaseInitializer
import com.wasmo.sql.VertxRowMetadataHack
import com.wasmo.sql.WasmoPostgresqlConfig
import com.wasmo.sql.asSqlDatabase

class FakeSqlService(
  val databaseName: String,
) : SqlService {
  /** Cached instance so [getOrCreate] is idempotent. */
  private var sqlDatabase: SqlDatabase? = null

  override suspend fun getOrCreate(name: String): SqlDatabase {
    require(name.isEmpty()) { "unexpected database name: $name" }

    val existing = sqlDatabase
    if (existing != null) return existing

    val config = WasmoPostgresqlConfig(
      hostname = System.getenv("POSTGRESQL_HOSTNAME") ?: "localhost",
      ssl = false,
      adminUser = "postgres",
      adminPassword = Secret("password"),
      adminDatabaseName = "postgres",
      osUser = databaseName,
      osPassword = Secret("password"),
      osDatabaseName = databaseName,
      appPrefix = "fake_sql_service",
    )

    val clientFactory = PostgresqlClient.Factory(
      vertxRowMetadataHack = VertxRowMetadataHack(),
      eventListener = { },
    )
    RealOsDatabaseInitializer(clientFactory, config).apply {
      initialize()
      dangerouslyClearSchema()
    }

    val client = clientFactory
      .connect(config.os)

    val result = client.asSqlDatabase()
    sqlDatabase = result
    return result
  }

  override fun close() {
    sqlDatabase?.close()
  }
}
