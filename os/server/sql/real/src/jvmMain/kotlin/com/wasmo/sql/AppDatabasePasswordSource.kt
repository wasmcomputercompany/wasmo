package com.wasmo.sql

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.DatabaseSlug
import com.wasmo.identifiers.InstalledAppScope
import com.wasmo.identifiers.Secret
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.ByteString.Companion.encodeUtf8

/**
 * Currently we just run a scheme based on os password information and computer + app information.
 *
 * We deem this safe because if an attacker happens to obtain the os password then they would
 * already be able to impersonate anyone on that machine.
 */
@Inject
@SingleIn(InstalledAppScope::class)
class AppDatabasePasswordSource(
  private val secret: AppDatabaseSecret,
  private val computerSlug: ComputerSlug,
  private val appSlug: AppSlug,
) {
  fun retrievePassword(databaseSlug: DatabaseSlug): Secret {
    val passwordSource = computerSlug.value + DELIMITER + appSlug.value + DELIMITER + databaseSlug.value
    val derivedPassword = passwordSource.encodeUtf8()
      .hmacSha256(secret.value)
      .base64()
    return Secret(derivedPassword)
  }

  companion object {
    const val DELIMITER = "\n"
  }
}
