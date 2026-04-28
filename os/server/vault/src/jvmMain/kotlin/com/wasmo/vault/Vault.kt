package com.wasmo.vault

import com.wasmo.identifiers.Ciphertext
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import okio.ByteString
import okio.ByteString.Companion.toByteString

/**
 * A simple AES/GCM encryption thing.
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

  fun encrypt(cleartext: ByteString): Ciphertext {
    val (keyId, key) = keys.entries.first()
    val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")

    val iv = ByteArray(12).apply {
      secureRandom.nextBytes(this)
    }

    val gcmCiphertext = Cipher.getInstance("AES/GCM/NoPadding").run {
      init(Cipher.ENCRYPT_MODE, secretKeySpec, GCMParameterSpec(128, iv))
      doFinal(cleartext.toByteArray())
    }

    return Ciphertext(
      keyId = keyId,
      iv = iv.toByteString(),
      gcmCiphertext = gcmCiphertext.toByteString(),
    )
  }

  fun decrypt(ciphertext: Ciphertext): ByteString {
    val keyId = ciphertext.keyId
    val key = keys[keyId] ?: throw IllegalArgumentException("unknown key: $keyId")
    val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")

    try {
      return Cipher.getInstance("AES/GCM/NoPadding").run {
        init(Cipher.DECRYPT_MODE, secretKeySpec, GCMParameterSpec(128, ciphertext.iv.toByteArray()))
        doFinal(ciphertext.gcmCiphertext.toByteArray()).toByteString()
      }
    } catch (_: GeneralSecurityException) {
      throw IllegalArgumentException("unexpected ciphertext")
    }
  }
}
