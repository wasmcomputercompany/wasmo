package com.wasmo.api

import com.wasmo.identifiers.Event

/**
 * Fired right before a SQL query or statement is executed.
 */
data class SqlExecuteStartedEvent(val query: String, val params: List<Any?>) : Event
