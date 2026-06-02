package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.test.assertFailsWith

class WitStructureReaderTest {

  @Test
  fun `charArray indexOf`() {
    assertThat("".toCharArray().indexOf("abc", 0)).isEqualTo(-1)
    assertThat("".toCharArray().indexOf("bc", 0)).isEqualTo(-1)
    assertThat("".toCharArray().indexOf("c", 0)).isEqualTo(-1)

    assertThat("abc".toCharArray().indexOf("abc", 0)).isEqualTo(0)
    assertThat("abc".toCharArray().indexOf("ab", 0)).isEqualTo(0)
    assertThat("abc".toCharArray().indexOf("a", 0)).isEqualTo(0)
    assertThat("abc".toCharArray().indexOf("bc", 0)).isEqualTo(1)
    assertThat("abc".toCharArray().indexOf("c", 0)).isEqualTo(2)
    assertThat("abc".toCharArray().indexOf("abcd", 0)).isEqualTo(-1)
    assertThat("abc".toCharArray().indexOf("bcd", 0)).isEqualTo(-1)
    assertThat("abc".toCharArray().indexOf("cd", 0)).isEqualTo(-1)
    assertThat("abc".toCharArray().indexOf("d", 0)).isEqualTo(-1)

    assertThat("abcabc".toCharArray().indexOf("abc", 3)).isEqualTo(3)
    assertThat("abcabc".toCharArray().indexOf("ab", 3)).isEqualTo(3)
    assertThat("abcabc".toCharArray().indexOf("a", 3)).isEqualTo(3)
    assertThat("abcabc".toCharArray().indexOf("bc", 3)).isEqualTo(4)
    assertThat("abcabc".toCharArray().indexOf("c", 3)).isEqualTo(5)
    assertThat("abcabc".toCharArray().indexOf("abcd", 3)).isEqualTo(-1)
    assertThat("abcabc".toCharArray().indexOf("bcd", 3)).isEqualTo(-1)
    assertThat("abcabc".toCharArray().indexOf("cd", 3)).isEqualTo(-1)
    assertThat("abcabc".toCharArray().indexOf("d", 3)).isEqualTo(-1)

    assertThat("aaa".toCharArray().indexOf("aaa", 0)).isEqualTo(0)
    assertThat("aaa".toCharArray().indexOf("aa", 0)).isEqualTo(0)
    assertThat("aaa".toCharArray().indexOf("a", 0)).isEqualTo(0)
    assertThat("aaa".toCharArray().indexOf("aaaa", 0)).isEqualTo(-1)
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
    assertThat(reader.takeDocumentation()).isEqualTo(Documentation("abc"))
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
  fun `dangling end of line documentation comment`() {
    val reader = WitStructureReader(
      """
      |
      |///abc
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.location).isEqualTo(Location(2, 7))
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
    assertThat(reader.takeDocumentation()).isEqualTo(Documentation("abc"))
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
    assertThat(reader.takeDocumentation()).isEqualTo(Documentation("abc\ndef"))
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
    assertThat(reader.takeDocumentation()).isEqualTo(Documentation("abc"))
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
    assertThat(reader.takeDocumentation()).isEqualTo(Documentation("abc"))
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
    assertThat(reader.takeDocumentation()).isEqualTo(Documentation("abc\n\ndef\nghi"))
    assertThat(reader.location).isEqualTo(Location(9, 1))
  }

  @Test
  fun `readIdentifier success`() {
    val reader = WitStructureReader(
      """
      |a abc def-ghi-xyz DEF-GHI-XYZ abc1234-ABC1234
      """.trimMargin(),
    )
    assertThat(reader.readIdentifier()).isEqualTo(Identifier("a"))
    reader.skipWhitespace()
    assertThat(reader.readIdentifier()).isEqualTo(Identifier("abc"))
    reader.skipWhitespace()
    assertThat(reader.readIdentifier()).isEqualTo(Identifier("def-ghi-xyz"))
    reader.skipWhitespace()
    assertThat(reader.readIdentifier()).isEqualTo(Identifier("DEF-GHI-XYZ"))
    reader.skipWhitespace()
    assertThat(reader.readIdentifier()).isEqualTo(Identifier("abc1234-ABC1234"))
  }

  @Test
  fun `readIdentifier crash`() {
    assertFailsWith<WitException> {
      WitStructureReader(" ").readIdentifier()
    }
    assertFailsWith<WitException> {
      WitStructureReader("_").readIdentifier()
    }
    assertFailsWith<WitException> {
      WitStructureReader("()").readIdentifier()
    }
    assertFailsWith<WitException> {
      WitStructureReader("").readIdentifier()
    }
  }

  @Test
  fun `readSemver success`() {
    val reader = WitStructureReader(
      """
      |1 1.0 1.2.3 1.0.0-alpha 1.0.0-alpha+001
      """.trimMargin(),
    )
    assertThat(reader.readSemVer()).isEqualTo(SemVer("1"))
    reader.skipWhitespace()
    assertThat(reader.readSemVer()).isEqualTo(SemVer("1.0"))
    reader.skipWhitespace()
    assertThat(reader.readSemVer()).isEqualTo(SemVer("1.2.3"))
    reader.skipWhitespace()
    assertThat(reader.readSemVer()).isEqualTo(SemVer("1.0.0-alpha"))
    reader.skipWhitespace()
    assertThat(reader.readSemVer()).isEqualTo(SemVer("1.0.0-alpha+001"))
  }

  @Test
  fun `readPackageName success`() {
    val reader = WitStructureReader(
      """
      |local:demo
      |examples:fgates-deprecation@0.2.0
      """.trimMargin(),
    )
    assertThat(reader.readPackageName()).isEqualTo(
      PackageName(
        namespace = "local",
        name = "demo",
      ),
    )
    reader.skipWhitespace()
    assertThat(reader.readPackageName()).isEqualTo(
      PackageName(
        namespace = "examples",
        name = "fgates-deprecation",
        version = "0.2.0",
      ),
    )
  }

  @Test
  fun `readPackageName multiple namespaces and multiple names`() {
    val reader = WitStructureReader("abc:def:ghi:jkl/mno/pqr")
    assertThat(reader.readPackageName()).isEqualTo(
      PackageName(
        namespaces = listOf(Identifier("abc"), Identifier("def"), Identifier("ghi")),
        names = listOf(Identifier("jkl"), Identifier("mno"), Identifier("pqr")),
      ),
    )
  }

  @Test
  fun `readPackageName missing namespace`() {
    val e = assertFailsWith<WitException> {
      WitStructureReader("local").readPackageName()
    }
    assertThat(e).hasMessage("expected package name to contain a ':'")
  }

  @Test
  fun `readPackageName empty namespace`() {
    val e = assertFailsWith<WitException> {
      WitStructureReader("a:").readPackageName()
    }
    assertThat(e).hasMessage("expected a word character")
  }

  @Test
  fun `readPackageName empty name`() {
    val e = assertFailsWith<WitException> {
      WitStructureReader(":").readPackageName()
    }
    assertThat(e).hasMessage("expected a word character")
  }

  @Test
  fun `readPackageName empty version`() {
    val e = assertFailsWith<WitException> {
      WitStructureReader("a:b@ ").readPackageName()
    }
    assertThat(e).hasMessage("expected a semver character")
  }
}
