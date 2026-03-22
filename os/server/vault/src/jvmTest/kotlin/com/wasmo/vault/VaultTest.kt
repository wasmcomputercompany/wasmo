package com.wasmo.vault

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertFailsWith
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.encodeUtf8

class VaultTest {
  private val k1 = "34d7a60fae596cdf52a7446457cb446e27563219a99b68e4072c48d5d0cd0da7".decodeHex()
  private val k2 = "f67860aa2db67057a75081f6c7226c68d67459c5892cc5040b9cc7b6f8e0c614".decodeHex()

  private val vaultWithK1 = Vault(
    mapOf(
      "k1" to k1,
    ),
  )
  private val vaultWithK2 = Vault(
    mapOf(
      "k2" to k2,
    ),
  )
  private val vaultWithK2K1 = Vault(
    mapOf(
      "k2" to k2,
      "k1" to k1,
    ),
  )

  @Test
  fun encryptDecrypt() {
    val cleartext = "This is the K1 secret message".encodeUtf8()
    val ciphertext = vaultWithK1.encrypt(cleartext)
    assertThat(vaultWithK1.decrypt(ciphertext)).isEqualTo(cleartext)
  }

  @Test
  fun cannotDecryptIfKeyAbsent() {
    val cleartext = "This is the K1 secret message".encodeUtf8()
    val ciphertext = vaultWithK1.encrypt(cleartext)
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        vaultWithK2.decrypt(ciphertext)
      },
    ).hasMessage("unknown key: k1")
  }

  @Test
  fun cannotDecryptIfTampered() {
    val cleartext = "This is the K1 secret message".encodeUtf8()
    val ciphertext = vaultWithK1.encrypt(cleartext)

    val malformed = Buffer().run {
      write(ciphertext, 0, ciphertext.size - 2)
      writeByte(ciphertext[ciphertext.size - 1] + 1)
      writeByte(ciphertext[ciphertext.size - 1].toInt())
      readByteString()
    }
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        vaultWithK1.decrypt(malformed)
      },
    ).hasMessage("unexpected ciphertext")
  }

  @Test
  fun cannotDecryptEmpty() {
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        vaultWithK1.decrypt(ByteString.EMPTY)
      },
    ).hasMessage("unexpected ciphertext")
  }

  @Test
  fun keyForwardsMigration() {
    val cleartext = "This is the K1 secret message".encodeUtf8()
    val ciphertext = vaultWithK1.encrypt(cleartext)
    assertThat(vaultWithK2K1.decrypt(ciphertext)).isEqualTo(cleartext)
  }

  @Test
  fun vaultsFirstListedKeyIsPreferred() {
    val cleartext = "This is the K1 secret message".encodeUtf8()
    val ciphertext = vaultWithK2K1.encrypt(cleartext)
    assertThat(vaultWithK2.decrypt(ciphertext)).isEqualTo(cleartext)
  }
}
