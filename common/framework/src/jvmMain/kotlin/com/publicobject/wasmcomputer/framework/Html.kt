package com.publicobject.wasmcomputer.framework

import kotlinx.html.HTML
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import okio.BufferedSink

fun BufferedSink.writeHtml(block: HTML.() -> Unit) {
  asAppendable().appendHTML(prettyPrint = false).html(block = block)
}

fun BufferedSink.asAppendable(): Appendable {
  return object : java.lang.Appendable {
    override fun append(csq: CharSequence) = apply {
      writeUtf8(csq.toString())
    }

    override fun append(
      csq: CharSequence,
      start: Int,
      end: Int,
    ) = apply {
      append(csq.substring(start, end))
    }

    override fun append(c: Char) = apply {
      writeUtf8CodePoint(c.code)
    }
  }
}
