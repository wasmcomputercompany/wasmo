package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.test.assertFailsWith

class WitStructureReaderTest {

  @Test
  fun `charArray indexOf`() {
    assertThat("".toCharArray().indexOf(0, "abc")).isEqualTo(-1)
    assertThat("".toCharArray().indexOf(0, "bc")).isEqualTo(-1)
    assertThat("".toCharArray().indexOf(0, "c")).isEqualTo(-1)

    assertThat("abc".toCharArray().indexOf(0, "abc")).isEqualTo(0)
    assertThat("abc".toCharArray().indexOf(0, "ab")).isEqualTo(0)
    assertThat("abc".toCharArray().indexOf(0, "a")).isEqualTo(0)
    assertThat("abc".toCharArray().indexOf(0, "bc")).isEqualTo(1)
    assertThat("abc".toCharArray().indexOf(0, "c")).isEqualTo(2)
    assertThat("abc".toCharArray().indexOf(0, "abcd")).isEqualTo(-1)
    assertThat("abc".toCharArray().indexOf(0, "bcd")).isEqualTo(-1)
    assertThat("abc".toCharArray().indexOf(0, "cd")).isEqualTo(-1)
    assertThat("abc".toCharArray().indexOf(0, "d")).isEqualTo(-1)

    assertThat("abcabc".toCharArray().indexOf(3, "abc")).isEqualTo(3)
    assertThat("abcabc".toCharArray().indexOf(3, "ab")).isEqualTo(3)
    assertThat("abcabc".toCharArray().indexOf(3, "a")).isEqualTo(3)
    assertThat("abcabc".toCharArray().indexOf(3, "bc")).isEqualTo(4)
    assertThat("abcabc".toCharArray().indexOf(3, "c")).isEqualTo(5)
    assertThat("abcabc".toCharArray().indexOf(3, "abcd")).isEqualTo(-1)
    assertThat("abcabc".toCharArray().indexOf(3, "bcd")).isEqualTo(-1)
    assertThat("abcabc".toCharArray().indexOf(3, "cd")).isEqualTo(-1)
    assertThat("abcabc".toCharArray().indexOf(3, "d")).isEqualTo(-1)

    assertThat("aaa".toCharArray().indexOf(0, "aaa")).isEqualTo(0)
    assertThat("aaa".toCharArray().indexOf(0, "aa")).isEqualTo(0)
    assertThat("aaa".toCharArray().indexOf(0, "a")).isEqualTo(0)
    assertThat("aaa".toCharArray().indexOf(0, "aaaa")).isEqualTo(-1)
  }

  @Test
  fun `skip whitespace and end of line comment`() {
    val reader = WitStructureReader(
      """
      |
      |//abc
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.location).isEqualTo(Location(4, 1))
  }

  @Test
  fun `skip whitespace and end of line documentation`() {
    val reader = WitStructureReader(
      """
      |
      |///abc
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isEqualTo("abc")
    assertThat(reader.location).isEqualTo(Location(4, 1))
  }

  @Test
  fun `dangling end of line comment`() {
    val reader = WitStructureReader(
      """
      |
      |//abc
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.location).isEqualTo(Location(2, 6))
  }

  @Test
  fun `document ends with end of line comment`() {
    val reader = WitStructureReader(
      """
      |//
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.location).isEqualTo(Location(1, 3))
  }

  @Test
  fun `end of line documentation`() {
    val reader = WitStructureReader(
      """
      |
      |///abc
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isEqualTo("abc")
    assertThat(reader.location).isEqualTo(Location(4, 1))
  }

  @Test
  fun `multiple lines of end of line documentation`() {
    val reader = WitStructureReader(
      """
      |
      |///abc
      |
      |///def
      |
      |g
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isEqualTo("abc\ndef")
    assertThat(reader.location).isEqualTo(Location(6, 1))
  }

  @Test
  fun `skip whitespace and asterisk comment`() {
    val reader = WitStructureReader(
      """
      |
      |/*abc*/
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.location).isEqualTo(Location(4, 1))
  }

  @Test
  fun `skip whitespace and asterisk documentation`() {
    val reader = WitStructureReader(
      """
      |
      |/**abc*/
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isEqualTo("abc")
    assertThat(reader.location).isEqualTo(Location(4, 1))
  }

  @Test
  fun `dangling asterisk comment`() {
    val reader = WitStructureReader(
      """
      |
      |/*abc
      """.trimMargin(),
    )
    val e = assertFailsWith<WitException> {
      reader.skipWhitespace()
    }
    assertThat(e.location).isEqualTo(Location(2, 1))
    assertThat(e.message).isEqualTo("unterminated comment")
  }

  @Test
  fun `document ends with asterisk comment`() {
    val reader = WitStructureReader(
      """
      |/* */
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.location).isEqualTo(Location(1, 6))
  }

  @Test
  fun `asterisk documentation`() {
    val reader = WitStructureReader(
      """
      |
      |/**abc*/
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isEqualTo("abc")
    assertThat(reader.location).isEqualTo(Location(4, 1))
  }

  @Test
  fun `multiple lines of asterisk documentation`() {
    val reader = WitStructureReader(
      """
      |
      |/**abc
      |
      |def*/
      |
      |
      |/**ghi*/
      |
      |g
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isEqualTo("abc\n\ndef\nghi")
    assertThat(reader.location).isEqualTo(Location(9, 1))
  }
}
