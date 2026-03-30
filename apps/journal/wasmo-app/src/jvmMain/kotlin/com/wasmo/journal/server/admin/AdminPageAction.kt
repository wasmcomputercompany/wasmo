package com.wasmo.journal.server.admin

import java.io.StringWriter
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.stream.appendHTML
import kotlinx.html.title
import kotlinx.html.unsafe
import okio.ByteString.Companion.encodeUtf8
import wasmo.http.Header
import wasmo.http.HttpResponse

class AdminPageAction {
  suspend fun admin(): HttpResponse {
    val appendable = StringWriter()
    appendable.write("<!DOCTYPE html>")
    appendable.appendHTML().html {
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
      body = appendable.toString().encodeUtf8(),
    )
  }

  companion object {
    val AdminHomePathRegex = Regex("/admin")
    val AdminEntryPathRegex = Regex("/admin/entries/([^/]+)")
  }
}
