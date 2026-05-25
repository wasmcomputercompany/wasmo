package com.wasmo.postgresqloperator

import com.wasmo.identifiers.Event

/** Human-readable CLI output. */
data class CliOutputEvent(
  val name: String,
  val stream: Stream,
  val message: String,
) : Event

enum class Stream {
  Stdout,
  Stderr,
}
