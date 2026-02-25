package com.wasmo.common.tokens

import java.security.SecureRandom

private val random = SecureRandom()

actual fun nextBytes(array: ByteArray) = random.nextBytes(array)
