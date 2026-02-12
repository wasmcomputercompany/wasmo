package com.wasmo.vault

import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.toByteString

/**
 * A simple AES/GCM encryption thing.
 *
 * Ciphertext is structured as three '\n' delimited parts.
 *
 * ```
 * <key ID>
 * <12 byte IV>
 * <GCM ciphertext>
 * ```
 *
 * @param keys the first secret will be used to encrypt new data. Other secrets will be used to
 *   decrypt if necessary. This is intended to permit gradual migration.
 */
class Vault(
  private val keys: Map<String, ByteString>,
) {
  private val secureRandom = SecureRandom()

  init {
    require(keys.keys.all { it.matches(Regex("\\w+")) }) {
      "secrets must be word characters \\w+"
    }
    require(keys.values.all { it.size == 32 }) {
      "keys must be 32 bytes"
    }
  }

  fun encrypt(cleartext: ByteString): ByteString {
    val (keyId, key) = keys.entries.first()
    val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")

    val iv = ByteArray(12).apply {
      secureRandom.nextBytes(this)
    }

    val gcmCiphertext = Cipher.getInstance("AES/GCM/NoPadding").run {
      init(Cipher.ENCRYPT_MODE, secretKeySpec, GCMParameterSpec(128, iv))
      doFinal(cleartext.toByteArray())
    }

    return Buffer().run {
      writeUtf8(keyId)
      writeByte('\n'.code)
      write(iv)
      writeByte('\n'.code)
      write(gcmCiphertext)
      readByteString()
    }
  }

  fun decrypt(ciphertext: ByteString): ByteString {
    val buffer = Buffer()
    buffer.write(ciphertext)
    val newlineIndex = buffer.indexOf('\n'.code.toByte())
    require(newlineIndex != -1L) { "unexpected ciphertext" }

    val keyId = buffer.readUtf8(newlineIndex)
    val key = keys[keyId] ?: throw IllegalArgumentException("unknown key: $keyId")
    val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")

    require(buffer.readByte() == '\n'.code.toByte()) {
      "unexpected ciphertext"
    }

    val iv = buffer.readByteArray(12)

    require(buffer.readByte() == '\n'.code.toByte()) {
      "unexpected ciphertext"
    }

    val gcmCiphertext = buffer.readByteArray()

    try {
      return Cipher.getInstance("AES/GCM/NoPadding").run {
        init(Cipher.DECRYPT_MODE, secretKeySpec, GCMParameterSpec(128, iv))
        doFinal(gcmCiphertext).toByteString()
      }
    } catch (_: GeneralSecurityException) {
      throw IllegalArgumentException("unexpected ciphertext")
    }
  }
}
