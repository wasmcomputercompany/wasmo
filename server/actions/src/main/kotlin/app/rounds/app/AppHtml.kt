package app.rounds.app

import app.rounds.account.api.WasmComputerJson
import app.rounds.framework.ContentTypes
import app.rounds.framework.MapPageData
import app.rounds.framework.Response
import app.rounds.framework.ResponseBody
import app.rounds.framework.write
import app.rounds.framework.writeHtml
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
    val pageData = MapPageData.Builder(WasmComputerJson)
      .build()

    writeUtf8("<!DOCTYPE html>")
    writeHtml {
      head {
        meta(charset = "utf-8")
        title("WASM COMPUTER")
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

        script(src = "/assets/rounds.js") {}

        pageData.write(this)

        script {
          unsafe {
            raw("""rounds.startOnLoad();""")
          }
        }
      }
      body {
      }
    }
  }
}
