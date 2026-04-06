package com.wasmo.journal.server.publishing

import com.wasmo.journal.db.Entry
import com.wasmo.support.okiohtml.writeHtml
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.title
import kotlinx.html.unsafe
import okio.Buffer

class SiteRenderer(
  val prettyPrint: Boolean,
) {
  fun renderEntryToHtml(entry: Entry): String {
    val buffer = Buffer()
    buffer.writeHtml(
      prettyPrint = prettyPrint,
    ) {
      head {
        meta(charset = "utf-8")
        title(entry.title)
      }
      body {
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
    return buffer.readUtf8()
  }
}
