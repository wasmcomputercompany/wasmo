package com.wasmo.journal.server.admin

import com.wasmo.support.okiohtml.writeHtml
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.html.unsafe
import okio.Buffer
import wasmo.http.Header
import wasmo.http.HttpResponse

class AdminPageAction {
  suspend fun admin(): HttpResponse {
    val buffer = Buffer()
    buffer.writeHtml {
      head {
        meta(charset = "utf-8")
        title("Journal")
        script(src = "/assets/admin.js") {}
        link(rel = "stylesheet", href = "/assets/admin.css")
        script {
          unsafe {
            raw("""admin.startOnLoad();""")
          }
        }
      }
      body {
      }
    }

    return HttpResponse(
      headers = listOf(
        Header("content-type", "text/html"),
      ),
      body = buffer.readByteString(),
    )
  }

  companion object {
    val AdminHomePathRegex = Regex("/admin")
    val AdminEntryPathRegex = Regex("/admin/entries/([^/]+)")
  }
}
