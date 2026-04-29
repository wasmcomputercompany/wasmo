package com.wasmo.sql

import io.vertx.sqlclient.SqlClient
import wasmo.sql.SqlConnection

/**
 * Punch through our tidy abstractions to expose the underlying [SqlClient] when that's what we
 * need.
 *
 * This is used to share a SQL transaction with our Absurd job queue.
 */
interface OsSqlConnection : SqlConnection {
  val sqlClient: SqlClient
}
