package com.wasmo.support.wit

class WitException(
  val location: Location,
  val issue: String,
) : IllegalStateException("$issue at $location")
