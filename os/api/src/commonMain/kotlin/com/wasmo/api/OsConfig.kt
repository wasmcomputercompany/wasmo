package com.wasmo.api

sealed interface OsConfig {
  val installAppsFromFileSystem: Boolean

  object Standard : OsConfig {
    override val installAppsFromFileSystem: Boolean
      get() = false
  }

  object DevMode : OsConfig {
    override val installAppsFromFileSystem: Boolean
      get() = true
  }
}
