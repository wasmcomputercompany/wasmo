package wasmo.sql

import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.r2dbc.asSqlService
import com.wasmo.sql.r2dbc.connectPostgresqlAsync
import com.wasmo.sql.r2dbc.executeVoid
import com.wasmo.sql.r2dbc.withConnection
import io.r2dbc.spi.ConnectionFactory

suspend fun testSqlService(
  databaseName: String,
  clearSchema: Boolean,
): SqlService {
  val connectionPool = connectPostgresqlAsync(
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

private suspend fun ConnectionFactory.clearSchema() {
  withConnection {
    executeVoid("DROP SCHEMA public CASCADE")
    executeVoid("CREATE SCHEMA public")
    executeVoid("GRANT ALL ON SCHEMA public TO postgres")
    executeVoid("GRANT ALL ON SCHEMA public TO public")
  }
}
