package wasmo.sql

import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.asSqlService
import com.wasmo.sql.connectVertxPostgresql
import com.wasmo.sql.execute
import com.wasmo.sql.useConnection
import io.vertx.sqlclient.Pool

suspend fun testSqlService(
  databaseName: String,
  clearSchema: Boolean,
): SqlService {
  val connectionPool = connectVertxPostgresql(
    PostgresqlAddress(
      databaseName = databaseName,
      user = "postgres",
      password = "password",
      hostname = "localhost",
      ssl = false,
    ),
  )
  if (clearSchema) {
    connectionPool.clearSchema()
  }
  return connectionPool.asSqlService()
}

suspend fun Pool.clearSchema() {
  useConnection {
    execute("DROP SCHEMA IF EXISTS public CASCADE")
    execute("CREATE SCHEMA public")
    execute("GRANT ALL ON SCHEMA public TO postgres")
    execute("GRANT ALL ON SCHEMA public TO public")
  }
}
