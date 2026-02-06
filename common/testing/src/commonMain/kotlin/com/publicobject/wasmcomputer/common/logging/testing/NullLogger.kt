package com.publicobject.wasmcomputer.common.logging.testing

import com.publicobject.wasmcomputer.common.logging.Logger

object NullLogger : Logger {
  override fun info(message: String, throwable: Throwable?) {
  }
}
