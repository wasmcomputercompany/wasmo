package com.wasmo.support.tokens

import java.security.SecureRandom

private val random = SecureRandom()

actual fun nextBytes(array: ByteArray) = random.nextBytes(array)
