package com.wasmo.testing.emails

import com.wasmo.sendemail.EmailMessage
import com.wasmo.support.tokens.ChallengeCode
import com.wasmo.support.tokens.toChallengeCodeOrNull

private val ExtractChallengeCodeRegex = Regex("\\QSign in to wasmo.com with code \\E(\\d{6})")

fun EmailMessage.extractChallengeCode(): ChallengeCode {
  val matchResult = ExtractChallengeCodeRegex.matchEntire(subject)
    ?: error("unexpected subject: ${subject}")
  return matchResult.groupValues[1].toChallengeCodeOrNull()!!
}

fun ChallengeCode.differentCode(): ChallengeCode =
  ((value).toInt() + 1_000_001).toString().substring(1).toChallengeCodeOrNull()!!
