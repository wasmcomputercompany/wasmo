package com.wasmo.support.absurd

import io.vertx.sqlclient.SqlClient
import okio.FileSystem
import okio.Path.Companion.toPath

/** This extremely destructive operation drops all absurd data. It may be useful for tests. */
suspend fun SqlClient.dangerouslyClearAbsurdSchema() {
  execute("DROP SCHEMA IF EXISTS absurd CASCADE")
  execute("CREATE SCHEMA absurd")
  execute("GRANT ALL ON SCHEMA absurd TO postgres")
  execute("GRANT ALL ON SCHEMA absurd TO public")
}

suspend fun SqlClient.initAbsurdSchema() {
  val absurdDotSql = FileSystem.RESOURCES.read("/absurd/sql/absurd.sql".toPath()) {
    readUtf8()
  }
  execute(absurdDotSql)
}
