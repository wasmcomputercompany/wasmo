package wasmo.sql

import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.r2dbc.asSqlService
import com.wasmo.sql.r2dbc.connectPostgresqlAsync
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import kotlinx.coroutines.reactive.awaitSingle

suspend fun testSqlService(
  databaseName: String,
  clearSchema: Boolean,
): SqlService {
  val dataSource = connectPostgresqlAsync(
    PostgresqlAddress(
      databaseName = databaseName,
      user = "postgres",
      password = "password",
      hostname = "localhost",
      ssl = false,
    ),
  )
  if (clearSchema) {
    dataSource.clearSchema()
  }
  return dataSource.asSqlService()
}

private suspend fun PostgresqlConnectionFactory.clearSchema() {
  with(create().awaitSingle()) {
    createStatement("DROP SCHEMA public CASCADE").execute().awaitSingle()
    createStatement("CREATE SCHEMA public").execute().awaitSingle()
    createStatement("GRANT ALL ON SCHEMA public TO postgres").execute().awaitSingle()
    createStatement("GRANT ALL ON SCHEMA public TO public").execute().awaitSingle()
    close().subscribe()
  }
}
