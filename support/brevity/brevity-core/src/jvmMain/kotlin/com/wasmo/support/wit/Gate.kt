package com.wasmo.support.wit

/**
 * Gates are like annotations syntactically.
 *
 * ```wit
 * @unstable(feature = fancier-foo)
 * @since(version = 0.2.0)
 * @deprecated(version = 0.2.2)
 * interface foo {}
 * ```
 */
data class Gate(
  val unstable: Identifier? = null,
  val since: SemVer? = null,
  val deprecated: SemVer? = null,
) {
  init {
    require(unstable != null || since != null || deprecated != null)
  }

  override fun toString() = buildString {
    if (unstable != null) {
      append("@unstable(feature = ")
      append(unstable)
      append(")")
    }
    if (since != null) {
      if (isNotEmpty()) append(" ")
      append("@since(version = ")
      append(since)
      append(")")
    }
    if (deprecated != null) {
      if (isNotEmpty()) append(" ")
      append("@deprecated(version = ")
      append(deprecated)
      append(")")
    }
  }
}
