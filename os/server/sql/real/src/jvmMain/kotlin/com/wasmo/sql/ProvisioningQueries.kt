package com.wasmo.sql

import wasmo.sql.SqlConnection

context(connection: SqlConnection)
internal suspend fun createBareAppUser(
  appUsername: String,
  appUserPassword: String,
) {
  connection.execute(
    """
    CREATE USER $appUsername
    WITH PASSWORD '$appUserPassword'
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT
    """,
  )
}

context(connection: SqlConnection)
internal suspend fun createAppDatabase(databaseName: String) {
  connection.execute(
    """
    CREATE DATABASE $databaseName WITH ENCODING = 'UTF8'
    """,
  )
}

context(connection: SqlConnection)
internal suspend fun restrictAppDatabaseAccess(databaseName: String) {
  connection.execute(
    """
    REVOKE CONNECT, TEMPORARY ON DATABASE $databaseName FROM PUBLIC
    """,
  )
}

context(connection: SqlConnection)
internal suspend fun grantAccessToAppUser(
  databaseName: String,
  appUsername: String,
) {
  connection.execute(
    """
    GRANT CONNECT ON DATABASE $databaseName TO $appUsername
    """,
  )
}

context(connection: SqlConnection)
internal suspend fun revokePublicSchemaAccess() {
  connection.execute(
    """
    REVOKE ALL ON SCHEMA public FROM PUBLIC
    """,
  )
}

context(connection: SqlConnection)
internal suspend fun grantSchemaAccessToAppUser(appUsername: String) {
  connection.execute(
    """
    GRANT USAGE, CREATE ON SCHEMA public TO $appUsername
    """,
  )
}
