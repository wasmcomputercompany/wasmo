package com.wasmo.journal.server.publishing

import com.wasmo.journal.db.Entry
import com.wasmo.support.okiohtml.writeHtml
import kotlinx.html.HtmlBlockTag
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.title
import kotlinx.html.unsafe
import okio.BufferedSink

class SiteRenderer(
  val prettyPrint: Boolean,
) {
  context(sink: BufferedSink)
  fun renderEntryList(
    entries: List<Entry>,
  ) {
    sink.writeHtml(
      prettyPrint = prettyPrint,
    ) {
      head {
        meta(charset = "utf-8")
        title("Journal")
      }
      body {
        for (entry in entries) {
          renderEntry(entry)
        }
      }
    }
  }

  context(sink: BufferedSink)
  fun renderEntryToHtml(entry: Entry) {
    sink.writeHtml(
      prettyPrint = prettyPrint,
    ) {
      head {
        meta(charset = "utf-8")
        title(entry.title)
      }
      body {
        renderEntry(entry)
      }
    }
  }

  private fun HtmlBlockTag.renderEntry(entry: Entry) {
    h1 {
      text(entry.title)
    }
    p {
      unsafe {
        raw(entry.body)
      }
    }
  }
}
