package com.wasmo.support.wit

class WitException(
  val location: Location,
  message: String,
) : IllegalStateException(message)
