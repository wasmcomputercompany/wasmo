@file:OptIn(ExperimentalContracts::class)

package com.wasmo.support.wit

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal class WitStructureReader(
  private val chars: CharArray,
) {
  constructor(string: String) : this(string.toCharArray())

  private var documentation: StringBuilder? = null
  private var pos = 0
  private var line = 0
  private var lineStart = 0

  val location: Location
    get() = Location(line + 1, pos - lineStart + 1)

  val exhausted: Boolean
    get() = pos == chars.size

  fun takeDocumentation(): Documentation? {
    return when {
      documentation != null -> {
        Documentation(documentation.toString())
          .also { documentation = null }
      }

      else -> null
    }
  }

  private fun appendDocumentation(startIndex: Int, endIndex: Int) {
    val documentation = when (val d = this.documentation) {
      null -> StringBuilder().also { this.documentation = it }
      else -> d.also { it.append("\n") }
    }
    documentation.appendRange(chars, startIndex, endIndex)
  }

  fun readIdentifier(): Identifier =
    Identifier(readToken("word", Char::isWordCharacter))

  fun readSemVer(): SemVer =
    SemVer(readToken("semver", Char::isSemverCharacter))

  fun readToken(name: String, match: (Char) -> Boolean): String {
    checkWit(match(peek())) {
      "expected a $name character"
    }

    val end = chars.indexOfNextNonMatch(pos + 1, match)
    val result = String(chars, pos, end - pos)
    pos = end
    return result
  }

  private fun peek(): Char = when {
    exhausted -> '\u0000'
    else -> chars[pos]
  }

  fun readLiteral(literal: Char) {
    checkWit(!exhausted) {
      "expected $literal but was EOF"
    }
    checkWit(chars[pos] == literal) {
      "expected $literal but was '${chars[pos]}'"
    }
    pos++
  }

  fun readPackageName(): PackageName {
    val start = pos
    val namespaces = mutableListOf<Identifier>()
    val names = mutableListOf<Identifier>()

    while (true) {
      val identifier = readIdentifier()
      if (peek() == ':') {
        pos++ // Consume ':'.
        namespaces += identifier
      } else {
        names += identifier
        if (peek() != '/') break
        pos++ // Consume '/'.
      }
    }

    val version = when {
      peek() == '@' -> {
        pos++ // Consume '@'.
        readSemVer()
      }

      else -> null
    }

    checkWit(namespaces.isNotEmpty()) {
      "expected package name to contain a ':': ${String(chars, start, pos - start)}"
    }

    return PackageName(
      namespaces = namespaces,
      names = names,
      version = version,
    )
  }

  fun skipWhitespace() {
    while (!exhausted) {
      val c = chars[pos]
      when (c) {
        ' ', '\t', '\r' -> {
          pos++
          continue
        }

        '\n' -> {
          pos++
          line++
          lineStart = pos
          continue
        }

        '/' if (pos + 1 < chars.size) -> {
          val afterSlash = chars[pos + 1]
          when (afterSlash) {
            '/' -> {
              val commentEnd = chars.indexOf("\n", pos)
              // If the comment starts with '///', it is documentation.
              if (pos + 2 < chars.size && chars[pos + 2] == '/') {
                appendDocumentation(pos + 3, commentEnd)
              }
              if (commentEnd == -1) {
                pos = chars.size
                return
              }
              pos = commentEnd + 1
              line++
              lineStart = pos
              continue
            }

            '*' -> {
              val commentEnd = chars.indexOf("*/", pos + 2)
              checkWit(commentEnd != -1) { "unterminated comment" }

              // If the comment starts with '/**', it is documentation.
              if (chars[pos + 2] == '*') {
                appendDocumentation(pos + 3, commentEnd)
              }
              for (j in pos until commentEnd + 2) {
                pos++
                if (chars[j] == '\n') {
                  line++
                  lineStart = pos
                }
              }
              continue
            }
          }
        }
      }

      return
    }
  }

  internal inline fun checkWit(value: Boolean, message: () -> String) {
    checkWit(location, value, message)
  }
}

internal inline fun checkWit(location: Location, value: Boolean, message: () -> String) {
  contract {
    returns() implies value
  }
  if (!value) {
    throw WitException(location, message())
  }
}

internal fun CharArray.indexOf(substring: String, fromIndex: Int): Int {
  require(substring.isNotEmpty())
  search@
  for (i in fromIndex..size - substring.length) {
    for ((j, c) in substring.withIndex()) {
      if (this[i + j] != c) continue@search
    }
    return i
  }
  return -1
}

internal fun CharArray.indexOfNextNonMatch(
  fromIndex: Int,
  match: (Char) -> Boolean,
): Int {
  for (i in fromIndex until size) {
    if (match(this[i])) continue
    else return i
  }
  return size
}

internal val Char.isWordCharacter: Boolean
  get() = when (this) {
    in 'a'..'z' -> true
    in 'A'..'Z' -> true
    in '0'..'9' -> true
    '-' -> true
    else -> false
  }

/** https://semver.org/spec/v2.0.0.html */
internal val Char.isSemverCharacter: Boolean
  get() = when (this) {
    in 'a'..'z' -> true
    in 'A'..'Z' -> true
    in '0'..'9' -> true
    '-', '+', '.' -> true
    else -> false
  }
