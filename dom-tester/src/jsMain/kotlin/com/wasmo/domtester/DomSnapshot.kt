package com.wasmo.domtester

import kotlinx.browser.document
import org.w3c.dom.HTMLBaseElement
import org.w3c.dom.HTMLElement
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
