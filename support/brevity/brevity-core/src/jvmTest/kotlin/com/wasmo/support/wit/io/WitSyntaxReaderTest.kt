@file:OptIn(WitCoreInternalApi::class)

package com.wasmo.support.wit.io

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.wasmo.support.wit.Documentation
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.Offset
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.SemVer
import com.wasmo.support.wit.WitCoreInternalApi
import com.wasmo.support.wit.WitException
import com.wasmo.support.wit.toIdentifier
import com.wasmo.support.wit.toPackageName
import com.wasmo.support.wit.toSemVer
import kotlin.test.Test
import kotlin.test.assertFailsWith

class WitSyntaxReaderTest {

  @Test
  fun `tryReadLiteral char success`() {
    val reader = WitSyntaxReader("a")
    assertThat(reader.tryReadLiteral('a')).isTrue()
    assertThat(reader.offset).isEqualTo(Offset(1, 2))
  }

  @Test
  fun `tryReadLiteral char wrong value`() {
    val reader = WitSyntaxReader("a")
    assertThat(reader.tryReadLiteral('b')).isFalse()
    assertThat(reader.offset).isEqualTo(Offset(1, 1))
  }

  @Test
  fun `tryReadLiteral char eof`() {
    val reader = WitSyntaxReader("")
    assertThat(reader.tryReadLiteral('a')).isFalse()
    assertThat(reader.offset).isEqualTo(Offset(1, 1))
  }

  @Test
  fun `tryReadLiteral string success`() {
    val reader = WitSyntaxReader("a")
    assertThat(reader.tryReadLiteral("a")).isTrue()
    assertThat(reader.offset).isEqualTo(Offset(1, 2))
  }

  @Test
  fun `tryReadLiteral string multiple characters success`() {
    val reader = WitSyntaxReader("abcd")
    assertThat(reader.tryReadLiteral("abc")).isTrue()
    assertThat(reader.offset).isEqualTo(Offset(1, 4))
  }

  @Test
  fun `tryReadLiteral string multiple characters wrong value`() {
    val reader = WitSyntaxReader("abcd")
    assertThat(reader.tryReadLiteral("abd")).isFalse()
    assertThat(reader.offset).isEqualTo(Offset(1, 1))
  }

  @Test
  fun `tryReadLiteral string wrong value`() {
    val reader = WitSyntaxReader("a")
    assertThat(reader.tryReadLiteral("b")).isFalse()
    assertThat(reader.offset).isEqualTo(Offset(1, 1))
  }

  @Test
  fun `tryReadLiteral string eof`() {
    val reader = WitSyntaxReader("")
    assertThat(reader.tryReadLiteral("a")).isFalse()
    assertThat(reader.offset).isEqualTo(Offset(1, 1))
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
    val reader = WitSyntaxReader(
      """
      |
      |//abc
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.offset).isEqualTo(Offset(4, 1))
  }

  @Test
  fun `skip whitespace and end of line documentation`() {
    val reader = WitSyntaxReader(
      """
      |
      |///abc
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isEqualTo(Documentation("abc"))
    assertThat(reader.offset).isEqualTo(Offset(4, 1))
  }

  @Test
  fun `dangling end of line comment`() {
    val reader = WitSyntaxReader(
      """
      |
      |//abc
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.offset).isEqualTo(Offset(2, 6))
  }

  @Test
  fun `dangling end of line documentation comment`() {
    val reader = WitSyntaxReader(
      """
      |
      |///abc
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.offset).isEqualTo(Offset(2, 7))
  }

  @Test
  fun `document ends with end of line comment`() {
    val reader = WitSyntaxReader(
      """
      |//
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.offset).isEqualTo(Offset(1, 3))
  }

  @Test
  fun `end of line documentation`() {
    val reader = WitSyntaxReader(
      """
      |
      |///abc
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isEqualTo(Documentation("abc"))
    assertThat(reader.offset).isEqualTo(Offset(4, 1))
  }

  @Test
  fun `multiple lines of end of line documentation`() {
    val reader = WitSyntaxReader(
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
    assertThat(reader.offset).isEqualTo(Offset(6, 1))
  }

  @Test
  fun `skip whitespace and asterisk comment`() {
    val reader = WitSyntaxReader(
      """
      |
      |/*abc*/
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.offset).isEqualTo(Offset(4, 1))
  }

  @Test
  fun `skip whitespace and asterisk documentation`() {
    val reader = WitSyntaxReader(
      """
      |
      |/**abc*/
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isEqualTo(Documentation("abc"))
    assertThat(reader.offset).isEqualTo(Offset(4, 1))
  }

  @Test
  fun `dangling asterisk comment`() {
    val reader = WitSyntaxReader(
      """
      |
      |/*abc
      """.trimMargin(),
    )
    val e = assertFailsWith<WitException> {
      reader.skipWhitespace()
    }
    assertThat(e.offset).isEqualTo(Offset(2, 1))
    assertThat(e.issue).isEqualTo("unterminated comment")
  }

  @Test
  fun `document ends with asterisk comment`() {
    val reader = WitSyntaxReader(
      """
      |/* */
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isNull()
    assertThat(reader.offset).isEqualTo(Offset(1, 6))
  }

  @Test
  fun `asterisk documentation`() {
    val reader = WitSyntaxReader(
      """
      |
      |/**abc*/
      |
      |d
      """.trimMargin(),
    )
    reader.skipWhitespace()
    assertThat(reader.takeDocumentation()).isEqualTo(Documentation("abc"))
    assertThat(reader.offset).isEqualTo(Offset(4, 1))
  }

  @Test
  fun `multiple lines of asterisk documentation`() {
    val reader = WitSyntaxReader(
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
    assertThat(reader.offset).isEqualTo(Offset(9, 1))
  }

  @Test
  fun `readIdentifier success`() {
    fun String.parseIdentifier() = WitSyntaxReader(this).readIdentifier()

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
      " ".toIdentifier()
    }
    assertFailsWith<WitException> {
      "_".toIdentifier()
    }
    assertFailsWith<WitException> {
      "()".toIdentifier()
    }
    assertFailsWith<WitException> {
      "".toIdentifier()
    }
    assertFailsWith<WitException> {
      "%%".toIdentifier()
    }
    assertFailsWith<WitException> {
      "%".toIdentifier()
    }
    assertFailsWith<WitException> {
      "% ".toIdentifier()
    }
  }

  @Test
  fun `readSemver success`() {
    assertThat("1".toSemVer()).isEqualTo(SemVer("1"))
    assertThat("1.0".toSemVer()).isEqualTo(SemVer("1.0"))
    assertThat("1.2.3".toSemVer()).isEqualTo(SemVer("1.2.3"))
    assertThat("1.0.0-alpha".toSemVer()).isEqualTo(SemVer("1.0.0-alpha"))
    assertThat("1.0.0-alpha+001".toSemVer()).isEqualTo(SemVer("1.0.0-alpha+001"))
  }

  @Test
  fun `readSemver omits trailing dots`() {
    val reader = WitSyntaxReader("1.2.")
    assertThat(reader.readSemVer()).isEqualTo(SemVer("1.2"))
    assertThat(reader.offset).isEqualTo(Offset(1, 4))
  }

  @Test
  fun `readSemver only dots`() {
    val e = assertFailsWith<WitException> {
      "..".toSemVer()
    }
    assertThat(e.issue).isEqualTo("expected a semver character")
  }

  @Test
  fun `readPackageName success`() {
    assertThat("local:demo".toPackageName()).isEqualTo(
      PackageName(
        namespaces = listOf(Identifier("local")),
        names = listOf(Identifier("demo")),
      ),
    )
    assertThat("examples:fgates-deprecation@0.2.0".toPackageName()).isEqualTo(
      PackageName(
        namespaces = listOf(Identifier("examples")),
        names = listOf(Identifier("fgates-deprecation")),
        version = SemVer("0.2.0"),
      ),
    )
  }

  @Test
  fun `readPackageName multiple namespaces and multiple names`() {
    assertThat("abc:def:ghi:jkl/mno/pqr".toPackageName()).isEqualTo(
      PackageName(
        namespaces = listOf(Identifier("abc"), Identifier("def"), Identifier("ghi")),
        names = listOf(Identifier("jkl"), Identifier("mno"), Identifier("pqr")),
      ),
    )
  }

  @Test
  fun `readPackageName missing namespace`() {
    val e = assertFailsWith<WitException> {
      "local".toPackageName()
    }
    assertThat(e.issue).isEqualTo("expected package name to contain a ':'")
  }

  @Test
  fun `readPackageName empty namespace`() {
    val e = assertFailsWith<WitException> {
      "a:".toPackageName()
    }
    assertThat(e.issue).isEqualTo("expected an identifier")
  }

  @Test
  fun `readPackageName empty name`() {
    val e = assertFailsWith<WitException> {
      ":".toPackageName()
    }
    assertThat(e.issue).isEqualTo("expected an identifier")
  }

  @Test
  fun `readPackageName empty version`() {
    val e = assertFailsWith<WitException> {
      "a:b@ ".toPackageName()
    }
    assertThat(e.issue).isEqualTo("expected a semver character")
  }

  @Test
  fun `readUsePath success`() {
    assertThat("my-interface".toUsePath()).isEqualTo(
      UsePath(name = Identifier("my-interface")),
    )
    assertThat("namespace:package-name/my-interface".toUsePath()).isEqualTo(
      UsePath(
        namespaces = listOf(Identifier("namespace")),
        packageNames = listOf(Identifier("package-name")),
        name = Identifier("my-interface"),
      ),
    )
    assertThat("namespace:package-name/my-interface@1.2.3".toUsePath()).isEqualTo(
      UsePath(
        namespaces = listOf(Identifier("namespace")),
        packageNames = listOf(Identifier("package-name")),
        name = Identifier("my-interface"),
        version = SemVer("1.2.3"),
      ),
    )
    assertThat("abc:def:ghi/jkl/my-interface".toUsePath()).isEqualTo(
      UsePath(
        namespaces = listOf(Identifier("abc"), Identifier("def")),
        packageNames = listOf(Identifier("ghi"), Identifier("jkl")),
        name = Identifier("my-interface"),
      ),
    )
    assertThat("abc:def:ghi/jkl/my-interface@1.2.3".toUsePath()).isEqualTo(
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
    val reader = WitSyntaxReader("my-interface@1.2.3")
    assertThat(reader.readUsePath()).isEqualTo(
      UsePath(name = Identifier("my-interface")),
    )
    assertThat(reader.offset).isEqualTo(Offset(1, 13)) // At the '@' symbol.
  }

  @Test
  fun `readUsePath namespace without package name`() {
    val e = assertFailsWith<WitException> {
      "namespace:interface-name".toUsePath()
    }
    assertThat(e.issue).isEqualTo("must have a namespace and a package name, or neither")
  }

  @Test
  fun `readUsePath package name without namespace`() {
    val e = assertFailsWith<WitException> {
      "package-name/interface-name".toUsePath()
    }
    assertThat(e.issue).isEqualTo("must have a namespace and a package name, or neither")
  }

  @Test
  fun `readTypeName success`() {
    assertThat("u32".toIoTypeName())
      .isEqualTo(IoTypeName.U32)
    assertThat("string".toIoTypeName())
      .isEqualTo(IoTypeName.String)
    assertThat("tuple<u32>".toIoTypeName())
      .isEqualTo(IoTypeName.Tuple(listOf(IoTypeName.U32)))
    assertThat("tuple<u32, s8>".toIoTypeName())
      .isEqualTo(IoTypeName.Tuple(listOf(IoTypeName.U32, IoTypeName.S8)))
    assertThat("tuple<u32, s8, string>".toIoTypeName())
      .isEqualTo(IoTypeName.Tuple(listOf(IoTypeName.U32, IoTypeName.S8, IoTypeName.String)))
    assertThat("list<string>".toIoTypeName())
      .isEqualTo(IoTypeName.List(IoTypeName.String))
    assertThat("list<string, 32>".toIoTypeName())
      .isEqualTo(IoTypeName.List(IoTypeName.String, 32U))
    assertThat("option<string>".toIoTypeName())
      .isEqualTo(IoTypeName.Option(IoTypeName.String))
    assertThat("result<string>".toIoTypeName())
      .isEqualTo(IoTypeName.Result(IoTypeName.String))
    assertThat("result<string, s32>".toIoTypeName())
      .isEqualTo(IoTypeName.Result(IoTypeName.String, IoTypeName.S32))
    assertThat("result<_, string>".toIoTypeName())
      .isEqualTo(IoTypeName.Result(null, IoTypeName.String))
    assertThat("result".toIoTypeName())
      .isEqualTo(IoTypeName.Result())
    assertThat("map<u32, s64>".toIoTypeName())
      .isEqualTo(IoTypeName.Map(IoTypeName.U32, IoTypeName.S64))
    assertThat("map<u32, list<string>>".toIoTypeName())
      .isEqualTo(IoTypeName.Map(IoTypeName.U32, IoTypeName.List(IoTypeName.String)))
    assertThat("future".toIoTypeName())
      .isEqualTo(IoTypeName.Future())
    assertThat("future<string>".toIoTypeName())
      .isEqualTo(IoTypeName.Future(IoTypeName.String))
    assertThat("borrow<string>".toIoTypeName())
      .isEqualTo(IoTypeName.Borrow(IoTypeName.String))
    assertThat("stream".toIoTypeName())
      .isEqualTo(IoTypeName.Stream())
    assertThat("stream<string>".toIoTypeName())
      .isEqualTo(IoTypeName.Stream(IoTypeName.String))
    assertThat("foo".toIoTypeName())
      .isEqualTo(IoTypeName.Declared("foo"))
  }

  @Test
  fun `readTypeName dangling type parameters`() {
    assertFailsWith<WitException> {
      "tuple<".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "tuple<string".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "tuple<string,".toIoTypeName()
    }
  }

  @Test
  fun `readTypeName invalid type parameters`() {
    assertFailsWith<WitException> {
      "tuple".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "tuple<>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "list".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "list<>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "list<string, string, string>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "option".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "option<>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "option<string, string>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "result<_, _>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "map<string>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "map<string, string, string>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "future<>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "future<string, string>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "stream<>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "stream<string, string>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "borrow".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "borrow<>".toIoTypeName()
    }
    assertFailsWith<WitException> {
      "borrow<string, string>".toIoTypeName()
    }
  }
}
