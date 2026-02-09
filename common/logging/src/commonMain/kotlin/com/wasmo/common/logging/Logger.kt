package com.wasmo.common.logging

interface Logger {
  fun info(message: String, throwable: Throwable? = null)
}
