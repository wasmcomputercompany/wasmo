package com.wasmo.api

sealed interface OsConfig {
  val installAppsFromFileSystem: Boolean

  val debugSqlQueries: Boolean

  object Standard : OsConfig {
    override val installAppsFromFileSystem: Boolean
      get() = false

    override val debugSqlQueries: Boolean
      get() = false
  }

  object DevMode : OsConfig {
    override val installAppsFromFileSystem: Boolean
      get() = true

    override val debugSqlQueries: Boolean
      get() = true
  }
}
