package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlin.test.assertFailsWith

class WitStructureReaderTest {

  @Test
  fun `tryReadLiteral char success`() {
    val reader = WitStructureReader("a")
    assertThat(reader.tryReadLiteral('a')).isTrue()
    assertThat(reader.location).isEqualTo(Location(1, 2))
  }

  @Test
  fun `tryReadLiteral char wrong value`() {
    val reader = WitStructureReader("a")
    assertThat(reader.tryReadLiteral('b')).isFalse()
    assertThat(reader.location).isEqualTo(Location(1, 1))
  }

  @Test
  fun `tryReadLiteral char eof`() {
    val reader = WitStructureReader("")
    assertThat(reader.tryReadLiteral('a')).isFalse()
    assertThat(reader.location).isEqualTo(Location(1, 1))
  }

  @Test
  fun `tryReadLiteral string success`() {
    val reader = WitStructureReader("a")
    assertThat(reader.tryReadLiteral("a")).isTrue()
    assertThat(reader.location).isEqualTo(Location(1, 2))
  }

  @Test
  fun `tryReadLiteral string multiple characters success`() {
    val reader = WitStructureReader("abcd")
    assertThat(reader.tryReadLiteral("abc")).isTrue()
    assertThat(reader.location).isEqualTo(Location(1, 4))
  }

  @Test
  fun `tryReadLiteral string multiple characters wrong value`() {
    val reader = WitStructureReader("abcd")
    assertThat(reader.tryReadLiteral("abd")).isFalse()
    assertThat(reader.location).isEqualTo(Location(1, 1))
  }

  @Test
  fun `tryReadLiteral string wrong value`() {
    val reader = WitStructureReader("a")
    assertThat(reader.tryReadLiteral("b")).isFalse()
    assertThat(reader.location).isEqualTo(Location(1, 1))
  }

  @Test
  fun `tryReadLiteral string eof`() {
    val reader = WitStructureReader("")
    assertThat(reader.tryReadLiteral("a")).isFalse()
    assertThat(reader.location).isEqualTo(Location(1, 1))
  }

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
    assertThat(e.issue).isEqualTo("unterminated comment")
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
    assertThat("a".parseIdentifier()).isEqualTo(Identifier("a"))
    assertThat("a ".parseIdentifier()).isEqualTo(Identifier("a"))
    assertThat("a,".parseIdentifier()).isEqualTo(Identifier("a"))
    assertThat("a)".parseIdentifier()).isEqualTo(Identifier("a"))
    assertThat("a}".parseIdentifier()).isEqualTo(Identifier("a"))
    assertThat("a:".parseIdentifier()).isEqualTo(Identifier("a"))
    assertThat("abc".parseIdentifier()).isEqualTo(Identifier("abc"))
    assertThat("abc ".parseIdentifier()).isEqualTo(Identifier("abc"))
    assertThat("def-ghi-xyz ".parseIdentifier()).isEqualTo(Identifier("def-ghi-xyz"))
    assertThat("DEF-GHI-XYZ ".parseIdentifier()).isEqualTo(Identifier("DEF-GHI-XYZ"))
    assertThat("abc1234-ABC1234 ".parseIdentifier()).isEqualTo(Identifier("abc1234-ABC1234"))
    assertThat("%a".parseIdentifier()).isEqualTo(Identifier("%a"))
    assertThat("%abc".parseIdentifier()).isEqualTo(Identifier("%abc"))
    assertThat("%abc%d".parseIdentifier()).isEqualTo(Identifier("%abc"))
  }

  @Test
  fun `readIdentifier crash`() {
    assertFailsWith<WitException> {
      " ".parseIdentifier()
    }
    assertFailsWith<WitException> {
      "_".parseIdentifier()
    }
    assertFailsWith<WitException> {
      "()".parseIdentifier()
    }
    assertFailsWith<WitException> {
      "".parseIdentifier()
    }
    assertFailsWith<WitException> {
      "%%".parseIdentifier()
    }
    assertFailsWith<WitException> {
      "%".parseIdentifier()
    }
    assertFailsWith<WitException> {
      "% ".parseIdentifier()
    }
  }

  @Test
  fun `readSemver success`() {
    assertThat("1".parseSemVer()).isEqualTo(SemVer("1"))
    assertThat("1.0".parseSemVer()).isEqualTo(SemVer("1.0"))
    assertThat("1.2.3".parseSemVer()).isEqualTo(SemVer("1.2.3"))
    assertThat("1.0.0-alpha".parseSemVer()).isEqualTo(SemVer("1.0.0-alpha"))
    assertThat("1.0.0-alpha+001".parseSemVer()).isEqualTo(SemVer("1.0.0-alpha+001"))
  }

  @Test
  fun `readSemver omits trailing dots`() {
    val reader = WitStructureReader("1.2.")
    assertThat(reader.readSemVer()).isEqualTo(SemVer("1.2"))
    assertThat(reader.location).isEqualTo(Location(1, 4))
  }

  @Test
  fun `readSemver only dots`() {
    val e = assertFailsWith<WitException> {
      "..".parseSemVer()
    }
    assertThat(e.issue).isEqualTo("expected a semver character")
  }

  @Test
  fun `readPackageName success`() {
    assertThat("local:demo".parsePackageName()).isEqualTo(
      PackageName(
        namespace = "local",
        name = "demo",
      ),
    )
    assertThat("examples:fgates-deprecation@0.2.0".parsePackageName()).isEqualTo(
      PackageName(
        namespace = "examples",
        name = "fgates-deprecation",
        version = "0.2.0",
      ),
    )
  }

  @Test
  fun `readPackageName multiple namespaces and multiple names`() {
    assertThat("abc:def:ghi:jkl/mno/pqr".parsePackageName()).isEqualTo(
      PackageName(
        namespaces = listOf(Identifier("abc"), Identifier("def"), Identifier("ghi")),
        names = listOf(Identifier("jkl"), Identifier("mno"), Identifier("pqr")),
      ),
    )
  }

  @Test
  fun `readPackageName missing namespace`() {
    val e = assertFailsWith<WitException> {
      "local".parsePackageName()
    }
    assertThat(e.issue).isEqualTo("expected package name to contain a ':'")
  }

  @Test
  fun `readPackageName empty namespace`() {
    val e = assertFailsWith<WitException> {
      "a:".parsePackageName()
    }
    assertThat(e.issue).isEqualTo("expected an identifier")
  }

  @Test
  fun `readPackageName empty name`() {
    val e = assertFailsWith<WitException> {
      ":".parsePackageName()
    }
    assertThat(e.issue).isEqualTo("expected an identifier")
  }

  @Test
  fun `readPackageName empty version`() {
    val e = assertFailsWith<WitException> {
      "a:b@ ".parsePackageName()
    }
    assertThat(e.issue).isEqualTo("expected a semver character")
  }

  @Test
  fun `readUsePath success`() {
    assertThat("my-interface".parseUsePath()).isEqualTo(
      UsePath(name = Identifier("my-interface")),
    )
    assertThat("namespace:package-name/my-interface".parseUsePath()).isEqualTo(
      UsePath(
        namespaces = listOf(Identifier("namespace")),
        packageNames = listOf(Identifier("package-name")),
        name = Identifier("my-interface"),
      ),
    )
    assertThat("namespace:package-name/my-interface@1.2.3".parseUsePath()).isEqualTo(
      UsePath(
        namespaces = listOf(Identifier("namespace")),
        packageNames = listOf(Identifier("package-name")),
        name = Identifier("my-interface"),
        version = SemVer("1.2.3"),
      ),
    )
    assertThat("abc:def:ghi/jkl/my-interface".parseUsePath()).isEqualTo(
      UsePath(
        namespaces = listOf(Identifier("abc"), Identifier("def")),
        packageNames = listOf(Identifier("ghi"), Identifier("jkl")),
        name = Identifier("my-interface"),
      ),
    )
    assertThat("abc:def:ghi/jkl/my-interface@1.2.3".parseUsePath()).isEqualTo(
      UsePath(
        namespaces = listOf(Identifier("abc"), Identifier("def")),
        packageNames = listOf(Identifier("ghi"), Identifier("jkl")),
        name = Identifier("my-interface"),
        version = SemVer("1.2.3"),
      ),
    )
  }

  @Test
  fun `readUsePath semver`() {
    val reader = WitStructureReader("my-interface@1.2.3")
    assertThat(reader.readUsePath()).isEqualTo(
      UsePath(name = Identifier("my-interface")),
    )
    assertThat(reader.location).isEqualTo(Location(1, 13)) // At the '@' symbol.
  }

  @Test
  fun `readUsePath namespace without package name`() {
    val e = assertFailsWith<WitException> {
      "namespace:interface-name".parseUsePath()
    }
    assertThat(e.issue).isEqualTo("must have a namespace and a package name, or neither")
  }

  @Test
  fun `readUsePath package name without namespace`() {
    val e = assertFailsWith<WitException> {
      "package-name/interface-name".parseUsePath()
    }
    assertThat(e.issue).isEqualTo("must have a namespace and a package name, or neither")
  }

  @Test
  fun `readTypeName success`() {
    assertThat("u32".parseTypeName())
      .isEqualTo(Types.u32)
    assertThat("string".parseTypeName())
      .isEqualTo(Types.string)
    assertThat("tuple<u32>".parseTypeName())
      .isEqualTo(TypeName.Tuple(listOf(Types.u32)))
    assertThat("tuple<u32, s8>".parseTypeName())
      .isEqualTo(TypeName.Tuple(listOf(Types.u32, Types.s8)))
    assertThat("tuple<u32, s8, string>".parseTypeName())
      .isEqualTo(TypeName.Tuple(listOf(Types.u32, Types.s8, Types.string)))
    assertThat("list<string>".parseTypeName())
      .isEqualTo(TypeName.List(Types.string))
    assertThat("list<string, 32>".parseTypeName())
      .isEqualTo(TypeName.List(Types.string, 32U))
    assertThat("option<string>".parseTypeName())
      .isEqualTo(TypeName.Option(Types.string))
    assertThat("result<string>".parseTypeName())
      .isEqualTo(TypeName.Result(Types.string))
    assertThat("result<string, s32>".parseTypeName())
      .isEqualTo(TypeName.Result(Types.string, Types.s32))
    assertThat("result<_, string>".parseTypeName())
      .isEqualTo(TypeName.Result(null, Types.string))
    assertThat("result".parseTypeName())
      .isEqualTo(TypeName.Result())
    assertThat("map<u32, s64>".parseTypeName())
      .isEqualTo(TypeName.Map(Types.u32, Types.s64))
    assertThat("map<u32, list<string>>".parseTypeName())
      .isEqualTo(TypeName.Map(Types.u32, TypeName.List(Types.string)))
    assertThat("future".parseTypeName())
      .isEqualTo(TypeName.Future())
    assertThat("future<string>".parseTypeName())
      .isEqualTo(TypeName.Future(Types.string))
    assertThat("borrow<string>".parseTypeName())
      .isEqualTo(TypeName.Borrow(Types.string))
    assertThat("stream".parseTypeName())
      .isEqualTo(TypeName.Stream())
    assertThat("stream<string>".parseTypeName())
      .isEqualTo(TypeName.Stream(Types.string))
    assertThat("foo".parseTypeName())
      .isEqualTo(TypeName("foo"))
  }

  @Test
  fun `readTypeName dangling type parameters`() {
    assertFailsWith<WitException> {
      "tuple<".parseTypeName()
    }
    assertFailsWith<WitException> {
      "tuple<string".parseTypeName()
    }
    assertFailsWith<WitException> {
      "tuple<string,".parseTypeName()
    }
  }

  @Test
  fun `readTypeName invalid type parameters`() {
    assertFailsWith<WitException> {
      "tuple".parseTypeName()
    }
    assertFailsWith<WitException> {
      "tuple<>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "list".parseTypeName()
    }
    assertFailsWith<WitException> {
      "list<>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "list<string, string, string>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "option".parseTypeName()
    }
    assertFailsWith<WitException> {
      "option<>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "option<string, string>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "result<_, _>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "map<string>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "map<string, string, string>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "future<>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "future<string, string>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "stream<>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "stream<string, string>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "borrow".parseTypeName()
    }
    assertFailsWith<WitException> {
      "borrow<>".parseTypeName()
    }
    assertFailsWith<WitException> {
      "borrow<string, string>".parseTypeName()
    }
  }

  private fun String.parseIdentifier(): Identifier =
    WitStructureReader(this).readIdentifier()

  private fun String.parsePackageName(): PackageName =
    WitStructureReader(this).readPackageName()

  private fun String.parseTypeName(): TypeName =
    WitStructureReader(this).readTypeName()

  private fun String.parseUsePath(): UsePath =
    WitStructureReader(this).readUsePath()

  private fun String.parseSemVer(): SemVer =
    WitStructureReader(this).readSemVer()
}
