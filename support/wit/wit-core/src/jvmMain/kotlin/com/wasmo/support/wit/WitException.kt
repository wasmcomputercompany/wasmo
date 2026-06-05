package com.wasmo.support.wit

class WitException(
  val issue: String,
  val path: String? = null,
  val location: Location? = null,
) : IllegalStateException(
  buildString {
    append(issue)
    if (location != null || path != null) {
      append(" at ")
    }
    if (path != null) {
      append(path)
    }
    if (location != null && path != null) {
      append(":")
    }
    if (location != null) {
      append(location)
    }
  },
)
