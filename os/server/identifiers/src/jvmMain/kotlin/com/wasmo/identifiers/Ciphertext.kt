package com.wasmo.identifiers

import okio.Buffer
import okio.ByteString

data class Ciphertext(
  val keyId: String,
  val iv: ByteString,
  val gcmCiphertext: ByteString,
)

/**
 * Ciphertext is structured as three '\n' delimited parts.
 *
 * ```
 * <key ID>
 * <12 byte IV>
 * <GCM ciphertext>
 * ```
 */
fun Ciphertext.encode(): ByteString {
  return Buffer().run {
    writeUtf8(keyId)
    writeByte('\n'.code)
    write(iv)
    writeByte('\n'.code)
    write(gcmCiphertext)
    readByteString()
  }
}

fun ByteString.decodeAsCiphertext(): Ciphertext {
  return decodeAsCiphertextOrNull()
    ?: throw IllegalArgumentException("unexpected ciphertext")
}

fun ByteString.decodeAsCiphertextOrNull(): Ciphertext? {
  val buffer = Buffer()
  buffer.write(this)

  val newlineIndex = buffer.indexOf('\n'.code.toByte())
  if (newlineIndex == -1L) return null
  val keyId = buffer.readUtf8(newlineIndex)

  if (buffer.readByte() != '\n'.code.toByte()) return null
  val iv = buffer.readByteString(12)

  if (buffer.readByte() != '\n'.code.toByte()) return null
  val gcmCiphertext = buffer.readByteString()

  return Ciphertext(
    keyId = keyId,
    iv = iv,
    gcmCiphertext = gcmCiphertext,
  )
}
