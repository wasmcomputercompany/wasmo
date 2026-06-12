package com.wasmo.sql.testing

import com.wasmo.identifiers.Secret
import com.wasmo.sql.WasmoPostgresqlConfig
import com.wasmo.sql.execute
import io.vertx.sqlclient.SqlClient
import okio.FileSystem
import okio.Path.Companion.toPath

val POSTGRESQL_HOSTNAME = System.getenv("POSTGRESQL_HOSTNAME")
  ?: "localhost"

fun testPostgresqlConfig(prefix: ProjectPrefix) = WasmoPostgresqlConfig(
  hostname = POSTGRESQL_HOSTNAME,
  ssl = false,
  adminUser = "postgres",
  adminPassword = Secret("password"),
  adminDatabaseName = "postgres",
  osUser = "${prefix}_wasmo_test",
  osPassword = Secret("password"),
  osDatabaseName = "${prefix}_wasmo_test",
  appPrefix = prefix.value,
)

suspend fun SqlClient.dropAppDatabases(prefix: ProjectPrefix) {
  val appDatabases = execute(
    """
    SELECT datname
    FROM pg_database
    WHERE datname like '${prefix}_app_%'
    """,
  )
  for (row in appDatabases) {
    val appDatabase = row.getString(0)
    // TODO: Figure out why we need "WITH (FORCE)". We shouldn't if it is cleaning up correctly.
    // Although maybe we leave it anyway.
    execute("DROP DATABASE IF EXISTS $appDatabase WITH (FORCE)")
  }
}

suspend fun SqlClient.dropAppRoles(prefix: ProjectPrefix) {
  val appRoles = execute(
    """
    SELECT rolname
    FROM pg_roles
    WHERE rolname like '${prefix}_app_%'
    """,
  )
  for (row in appRoles) {
    val appRole = row.getString(0)
    execute("DROP ROLE IF EXISTS $appRole")
  }
}

/**
 * A unique prefix for the currently-executing Gradle module. This allows us to run multiple Gradle
 * modules' tests in parallel, each on their own Postgresql database.
 *
 * Prefix strings look like "os_serv_inst_real" and contain up to 4 characters of each directory
 * segment in the current module's path.
 */
@JvmInline
value class ProjectPrefix(
  val value: String,
) {
  override fun toString() = value

  companion object {
    fun detect(): ProjectPrefix {
      val workingDirectory = FileSystem.SYSTEM.canonicalize(".".toPath())
      val segments = workingDirectory.segments
      val relativeSegments = segments.subList(segments.lastIndexOf("wasmo") + 1, segments.size)
      val prefix = relativeSegments.joinToString(separator = "_") { it.take(4) }
      return ProjectPrefix(prefix)
    }
  }
}
