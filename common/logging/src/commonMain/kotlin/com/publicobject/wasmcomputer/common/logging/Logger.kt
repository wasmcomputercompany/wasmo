package com.publicobject.wasmcomputer.common.logging

interface Logger {
  fun info(message: String, throwable: Throwable? = null)
}
