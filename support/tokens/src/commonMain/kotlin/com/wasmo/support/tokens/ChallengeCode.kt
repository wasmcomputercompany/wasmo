package com.wasmo.support.tokens

import kotlin.jvm.JvmInline

@JvmInline
value class ChallengeCode(val value: String)

fun String.toChallengeCodeOrNull(): ChallengeCode? {
  val digitsOnly = buildString {
    for (char in this@toChallengeCodeOrNull) {
      if (char !in '0'..'9') continue
      append(char)
    }
  }

  return when (digitsOnly.length) {
    6 -> ChallengeCode(this)
    else -> null
  }
}
