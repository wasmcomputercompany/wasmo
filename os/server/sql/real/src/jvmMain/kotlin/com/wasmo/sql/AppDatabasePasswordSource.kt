package com.wasmo.sql

import com.wasmo.identifiers.DatabaseSlug
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.Secret
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Currently we just run a scheme based on os password information and the app information.
 *
 * We deem this safe because if an attacker happens to obtain the os password then they would
 * already be able to impersonate anyone on that machine.
 */
@Inject
@SingleIn(OsScope::class)
class AppDatabasePasswordSource(
  private val config: WasmoPostgresqlConfig,
) {
  fun retrievePassword(databaseSlug: DatabaseSlug): Secret {
    //TODO: Actually generate something here.
    return Secret("app-password")
  }

}
