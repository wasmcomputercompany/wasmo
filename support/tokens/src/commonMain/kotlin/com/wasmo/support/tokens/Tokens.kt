package com.wasmo.support.tokens

private const val base32 = "0123456789abcdefghjkmnpqrstvwxyz"

const val TokenLength = 25

fun newToken(): String {
  val array = ByteArray(TokenLength)
  nextBytes(array)
  return buildString {
    for (value in array) {
      append(base32[value.toInt() and 0x1f])
    }
  }
}

expect fun nextBytes(array: ByteArray)
