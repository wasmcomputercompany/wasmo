package com.wasmo.db;

import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
abstract class DefaultMigratorBindings {
  companion object {
    @Provides
    @SingleIn(OsScope::class)
    fun provideMigrator (factory: RealMigrator.Factory): Migrator =
      factory.create(customPostUpgradeSteps = mapOf())
  }
}
