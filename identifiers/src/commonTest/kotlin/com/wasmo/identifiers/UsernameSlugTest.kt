package com.wasmo.identifiers

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UsernameSlugTest {

  @Test
  fun `Usernames must be between 3 and 20 characters long`() {
    assertThat(isUsernameValid("")).isFalse()
    assertThat(isUsernameValid("a")).isFalse()
    assertThat(isUsernameValid("ab")).isFalse()

    assertThat(isUsernameValid("aac")).isTrue()
    assertThat(isUsernameValid("aaaa")).isTrue()
    assertThat(isUsernameValid("a".repeat(19))).isTrue()
    assertThat(isUsernameValid("a".repeat(20))).isTrue()

    assertThat(isUsernameValid("a".repeat(21))).isFalse()
    assertThat(isUsernameValid("a".repeat(22))).isFalse()
    assertThat(isUsernameValid("a".repeat(10000))).isFalse()
  }

  @Test
  fun `Uppercase and punctuation characters are preserved but stripped from DB form`() {
    val username = UsernameSlug("J_o_h_n.D-o-e")
    assertEquals("J_o_h_n.D-o-e", username.value)
    assertEquals("johndoe", username.normalizedValue)
  }

  @Test
  fun `No punctuation characters in first or last position`() {
    assertThat(isUsernameValid("-JohnDoe")).isFalse()
    assertThat(isUsernameValid("_JohnDoe")).isFalse()
    assertThat(isUsernameValid(".JohnDoe")).isFalse()
    assertThat(isUsernameValid("JohnDoe-")).isFalse()
    assertThat(isUsernameValid("JohnDoe_")).isFalse()
    assertThat(isUsernameValid("JohnDoe.")).isFalse()

    // For comparison, these are valid
    assertThat(isUsernameValid("John.Doe")).isTrue()
    assertThat(isUsernameValid("John.doE")).isTrue()
  }

  @Test
  fun `No more than one punctuation character in a row`() {
    assertThat(isUsernameValid("John--Doe")).isFalse()
    assertThat(isUsernameValid("John__Doe")).isFalse()
    assertThat(isUsernameValid("John...Doe")).isFalse()
    assertThat(isUsernameValid("John-_Doe")).isFalse()
    assertThat(isUsernameValid("John._Doe")).isFalse()
    assertThat(isUsernameValid("John-._Doe")).isFalse()

    // For comparison, these are valid
    assertThat(isUsernameValid("John.Doe")).isTrue()
    assertThat(isUsernameValid("J-o-h-n_D.o.e")).isTrue()
    assertThat(isUsernameValid("John_Doe")).isTrue()
  }

  @Test
  fun `Sequences of digits 0-9 are allowed except in the first position`() {
    assertThat(isUsernameValid("jesse123")).isTrue()
    assertThat(isUsernameValid("jesse.123")).isTrue()
    assertThat(isUsernameValid("j01e23s45s67e89")).isTrue()
    assertThat(isUsernameValid("jesse123Wilson")).isTrue()

    assertThat(isUsernameValid("1jesse")).isFalse()
    assertThat(isUsernameValid("42jesse")).isFalse()
  }

  @Test
  fun `No empty string`() {
    assertThat(isUsernameValid("")).isFalse()
  }

  @Test
  fun `Constructor throws IAE on invalid username`() {
    val invalidUsernameString = "1"

    assertFailsWith<IllegalArgumentException> { UsernameSlug(invalidUsernameString) }

    // Check this test's assumptions
    assertThat(isUsernameValid(invalidUsernameString)).isFalse()
  }

  @Test
  fun `no accents or umlauts`() {
    // accents
    assertThat(isUsernameValid("quéstionable")).isFalse()
    assertThat(isUsernameValid("quëstionable")).isFalse()
    assertThat(isUsernameValid("quèstionable")).isFalse()

    // umlauts
    assertThat(isUsernameValid("questionÄble")).isFalse()
    assertThat(isUsernameValid("questionäble")).isFalse()
    assertThat(isUsernameValid("questiÖnable")).isFalse()
    assertThat(isUsernameValid("questiönable")).isFalse()
    assertThat(isUsernameValid("qÜestionable")).isFalse()
    assertThat(isUsernameValid("qüestionable")).isFalse()

    // Other than the illegal characters, it would have been fine
    assertThat(isUsernameValid("questionable")).isTrue()
  }

  // If the normalization algorithm changes, we need a DB migration to persisted normalized
  // values.
  @Test
  fun `Normalization algorithm has not changed`() {
    assertEquals("john123doe", UsernameSlug("J_o.H-n.1-23D_oE").normalizedValue)
  }

  @Test
  fun `toString equals and hashCode are based on String value`() {
    val username = UsernameSlug("jesse.123")
    assertThat(username.hashCode()).isEqualTo("jesse.123".hashCode())
    assertThat(username).isEqualTo(UsernameSlug("jesse.123"))
  }

  @Test
  fun `equals is based on value not normalizedValue`() {
    val username = UsernameSlug("jesse.123")
    val other = UsernameSlug("jesse123")
    // check an assumption of this test
    assertThat(username.normalizedValue).isEqualTo(other.normalizedValue)

    assertThat(username).isEqualTo(UsernameSlug("jesse.123"))
    assertThat(username).isNotEqualTo(other)
  }

}
