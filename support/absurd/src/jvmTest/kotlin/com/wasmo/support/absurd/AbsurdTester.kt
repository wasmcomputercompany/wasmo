package com.wasmo.support.absurd

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.client.SSLMode

class AbsurdTester(
  private val postgres: Postgres,
) {
  suspend fun clearSchema() {
    postgres.withConnection {
      execute("DROP SCHEMA public CASCADE")
      execute("CREATE SCHEMA public")
      execute("GRANT ALL ON SCHEMA public TO postgres")
      execute("GRANT ALL ON SCHEMA public TO public")
    }
  }

  companion object {
    fun create(): AbsurdTester {
      val configuration = PostgresqlConnectionConfiguration.builder()
        .host("localhost")
        .username("postgres")
        .password("password")
        .database("absurd_test")
        .sslMode(SSLMode.DISABLE)
        .build()
      return AbsurdTester(Postgres(PostgresqlConnectionFactory(configuration)))
    }
  }
}
