package com.wasmo.framework

import kotlinx.html.HTML
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import okio.BufferedSink

fun BufferedSink.writeHtml(block: HTML.() -> Unit) {
  asAppendable().appendHTML(prettyPrint = false).html(block = block)
}

fun BufferedSink.asAppendable(): Appendable {
  return object : Appendable {
    override fun append(value: Char) = apply {
      writeUtf8CodePoint(value.code)
    }

    override fun append(value: CharSequence?) = apply {
      if (value == null) return@apply
      writeUtf8(value.toString())
    }

    override fun append(
      value: CharSequence?,
      startIndex: Int,
      endIndex: Int,
    ) = apply {
      if (value == null) return@apply
      append(value.substring(startIndex, endIndex))
    }
  }
}
