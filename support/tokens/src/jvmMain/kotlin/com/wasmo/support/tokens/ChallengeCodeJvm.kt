package com.wasmo.support.tokens

fun newChallengeCode(): ChallengeCode =
  ChallengeCode(random.nextInt(1_000_000).toString().padStart(6, '0'))
