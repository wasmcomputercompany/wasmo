package com.wasmo.distributions.homelab

import com.wasmo.db.RealMigrator
import com.wasmo.db.NamedSchema
import com.wasmo.db.accounts.insertAccount
import com.wasmo.db.usernames.insertUsername
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.UsernameSlug
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@Inject
@SingleIn(OsScope::class)
internal class RealHomelabMigrator(clock: Clock) : RealMigrator(
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
