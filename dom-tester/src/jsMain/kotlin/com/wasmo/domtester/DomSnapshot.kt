package com.wasmo.domtester

import kotlinx.browser.document
import org.w3c.dom.HTMLBaseElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLMetaElement
import org.w3c.files.Blob

/**
 * A image rendering of an HTML element.
 */
data class DomSnapshot(
  val images: List<Blob?>,
  val elementHtml: String,
) {
  /** Returns a complete HTML page wrapping the HTML of this snapshot. */
  fun htmlPage(
    title: String,
    stylesheetsUrls: List<String>,
    baseHref: String,
  ): String = (document.createElement("html") as HTMLElement).run {
    append(
      document.createElement("head").apply {
        append(
          document.createElement("meta").apply {
            setAttribute("charset", "utf-8")
          },
          (document.createElement("meta") as HTMLMetaElement).apply {
            name = "viewport"
            content = "width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1"
          },
          document.createElement("title").apply {
            textContent = title
          },
          (document.createElement("base") as HTMLBaseElement).apply {
            href = baseHref
          },
        )
      },
      document.createElement("template").run {
        innerHTML = elementHtml
        asDynamic().content.firstChild
      },
    )

    addStylesheets(stylesheetsUrls)

    outerHTML
  }
}
