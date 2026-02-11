package com.wasmo.home

import com.wasmo.api.WasmoJson
import com.wasmo.framework.ContentTypes
import com.wasmo.framework.MapPageData
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.write
import com.wasmo.framework.writeHtml
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.html.unsafe
import okio.BufferedSink

class AppPage() : ResponseBody {
  val response: Response<ResponseBody>
    get() = Response(
      contentType = ContentTypes.TextHtml,
      body = this,
    )

  override fun write(sink: BufferedSink) = sink.run {
    val pageData = MapPageData.Builder(WasmoJson)
      .build()

    writeUtf8("<!DOCTYPE html>")
    writeHtml {
      head {
        meta(charset = "utf-8")
        title("Wasmo")
        meta(
          name = "viewport",
          content = "width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1",
        )
        meta(
          name = "theme-color",
          content = "#ffffff",
        )

        link(rel = "preconnect", href = "https://fonts.gstatic.com") {
          attributes["crossorigin"] = "anonymous"
        }
        link(
          rel = "stylesheet",
          href = "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
        )
        link(rel = "stylesheet", href = "/assets/Wasmo.css")

        script(src = "/assets/wasmo.js") {}

        pageData.write(this)

        script {
          unsafe {
            raw("""wasmo.startOnLoad();""")
          }
        }
      }
      body {
      }
    }
  }
}
