package com.wasmo.postgresqloperator

import com.wasmo.identifiers.Event

/**
 * A decoded Postgresql log message.
 *
 * https://www.postgresql.org/docs/current/runtime-config-logging.html#GUC-LOG-LINE-PREFIX
 */
data class PostgresqlLogEvent(
  val user: String? = null,
  val database: String? = null,
  val sqlStateErrorCode: String? = null,
  /** https://www.postgresql.org/docs/current/runtime-config-logging.html#RUNTIME-CONFIG-SEVERITY-LEVELS */
  val severity: String? = "LOG",
  val message: String,
) : Event
