package com.wasmo.support.wit

class WitException(
  val issue: String,
  val path: String? = null,
  val offset: Offset? = null,
) : IllegalStateException(
  buildString {
    append(issue)
    if (offset != null || path != null) {
      append(" at ")
    }
    if (path != null) {
      append(path)
    }
    if (offset != null && path != null) {
      append(":")
    }
    if (offset != null) {
      append(offset)
    }
  },
)
