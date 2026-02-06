package com.publicobject.wasmcomputer.common.tokens

actual fun nextBytes(array: ByteArray) = crypto.getRandomValues(array)

private external object crypto {
  fun getRandomValues(array: ByteArray)
}
