package com.wasmo.identifiers

import kotlinx.serialization.Serializable

/**
 * Shortname codes for different distributions for component naming.
 * Databases...
 * Example codes:
 *   HomeLab/localhost = "hl"
 *   Hosted/wasmo.com = "wc"
 *   Sandbox/wasmo.dev = "wd"
 *   Sdk = "sd"
 *   Test = "ft"
 */
@Serializable
@JvmInline
value class DistributionShortCode(val shortCode: String) {
  init {
    require(shortCode.matches(DistributionShortCodeRegex)) {
      "unexpected computer: $shortCode"
    }
  }

  override fun toString() = shortCode
}

/** Two letters. */
val DistributionShortCodeRegex = Regex("[a-z]{2}")

