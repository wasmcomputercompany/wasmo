package com.wasmo.common.logging

import com.wasmo.issues.Issue

interface Logger {
  fun info(message: String, issues: List<Issue>)
  fun info(message: String, throwable: Throwable? = null)
}
