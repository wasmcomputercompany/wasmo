package wasmo.sql

import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.asSqlService
import com.wasmo.sql.execute
import com.wasmo.sql.use
import io.vertx.sqlclient.SqlClient

suspend fun testSqlService(
  databaseName: String,
  clearSchema: Boolean,
): SqlService {
  val postgresqlAddress = PostgresqlAddress(
    databaseName = databaseName,
    user = "postgres",
    password = "password",
    hostname = "localhost",
    ssl = false,
  )
  postgresqlAddress.use { connection ->
    if (clearSchema) {
      connection.clearSchema()
    }
  }
  return postgresqlAddress.asSqlService()
}

suspend fun SqlClient.clearSchema() {
  execute("DROP SCHEMA IF EXISTS public CASCADE")
  execute("CREATE SCHEMA public")
  execute("GRANT ALL ON SCHEMA public TO postgres")
  execute("GRANT ALL ON SCHEMA public TO public")
}
