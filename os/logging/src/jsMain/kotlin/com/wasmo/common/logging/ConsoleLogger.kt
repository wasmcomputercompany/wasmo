package com.wasmo.common.logging

import com.wasmo.issues.Issue

object ConsoleLogger : Logger {
  override fun info(message: String, issues: List<Issue>) =
    console.log(message, *issues.toTypedArray())

  override fun info(message: String, throwable: Throwable?) {
    if (throwable != null) {
      console.log(message, throwable)
    } else {
      console.log(message)
    }
  }
}
