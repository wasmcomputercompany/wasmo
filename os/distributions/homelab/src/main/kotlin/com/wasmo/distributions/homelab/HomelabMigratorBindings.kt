package com.wasmo.distributions.homelab

import com.wasmo.db.Migrator
import com.wasmo.db.NamedSchema
import com.wasmo.db.RealMigrator
import com.wasmo.db.accounts.insertAccount
import com.wasmo.db.usernames.insertUsername
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.UsernameSlug
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@BindingContainer
abstract class HomelabMigratorBindings {
  companion object {
    @Provides
    @SingleIn(OsScope::class)
    fun provideRealHomelabMigrator(clock: Clock, factory: RealMigrator.Factory) : Migrator =
     factory.create(
       customPostUpgradeSteps = mapOf(
         NamedSchema.USERNAME.version to {
           val accountId = insertAccount(version = 1)
           insertUsername(
             createdAt = clock.now(),
             accountId = accountId,
             usernameSlug = UsernameSlug("admin"),
           )
         }
       )
     )
  }
}
