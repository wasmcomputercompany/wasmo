package com.wasmo.support.tokens

import java.security.SecureRandom

internal val random = SecureRandom()

actual fun nextBytes(array: ByteArray) = random.nextBytes(array)
