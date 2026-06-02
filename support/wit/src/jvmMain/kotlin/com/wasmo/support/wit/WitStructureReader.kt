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

  fun takeDocumentation(): String? {
    return when {
      documentation != null -> {
        documentation.toString()
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

  fun skipWhitespace() {
    while (pos < chars.size) {
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
              val commentEnd = chars.indexOf(pos, "\n")
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
              val commentEnd = chars.indexOf(pos + 2, "*/")
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
    contract {
      returns() implies value
    }
    if (!value) {
      throw WitException(location, message())
    }
  }
}

internal fun CharArray.indexOf(fromIndex: Int, substring: String): Int {
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
