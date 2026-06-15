package com.wasmo.api

/**
 * Listener for events triggered in the SQL service.
 */
fun interface SqlEventListener {
  fun onEvent(sqlEvent: SqlEvent)

  sealed interface SqlEvent {
    /**
     * Fired right before a SQL query or statement is executed.
     */
    data class SqlStarted(val query: String, val params: List<Any?>) : SqlEvent
  }
}
