package com.wasmo.support.tokens

fun newChallengeCode(): String =
  random.nextInt(1_000_000).toString().padStart(6, '0')
