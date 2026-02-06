package com.publicobject.wasmcomputer.common.tokens

private const val base32 = "0123456789abcdefghjkmnpqrstvwxyz"

fun newToken(): String {
  val array = ByteArray(25)
  nextBytes(array)
  return buildString {
    for (value in array) {
      append(base32[value.toInt() and 0x1f])
    }
  }
}

expect fun nextBytes(array: ByteArray)
