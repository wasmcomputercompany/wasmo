package com.wasmo.common.logging.testing

import com.wasmo.common.logging.Logger

object NullLogger : Logger {
  override fun info(message: String, throwable: Throwable?) {
  }
}
