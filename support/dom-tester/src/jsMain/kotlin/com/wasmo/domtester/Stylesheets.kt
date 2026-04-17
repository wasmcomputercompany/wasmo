package com.wasmo.domtester

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLinkElement
import org.w3c.dom.HTMLStyleElement
import org.w3c.dom.get

/**
 * Customize the page to never measure the scrollbar gutter. Otherwise, it's derived from the host
 * computer and therefore environment-dependent.
 */
val DomTesterStylesheet = """
  * {
    scrollbar-width: none;
    scrollbar-gutter: stable both-edges;
  }
  """

internal fun HTMLElement.addStylesheetUrls(
  stylesheetsUrls: List<String>,
): List<HTMLLinkElement> {
  val result = mutableListOf<HTMLLinkElement>()
  for (stylesheetUrl in stylesheetsUrls) {
    result += addStylesheetUrl(stylesheetUrl)
  }
  return result
}

internal fun HTMLElement.addStylesheetUrl(href: String): HTMLLinkElement {
  val head = getElementsByTagName("head").get(0)!!
  val stylesheet = (document.createElement("link") as HTMLLinkElement).apply {
    setAttribute("href", href)
    setAttribute("rel", "stylesheet")
  }
  head.appendChild(stylesheet)
  return stylesheet
}

internal fun HTMLElement.addStylesheetText(content: String): HTMLStyleElement {
  val head = getElementsByTagName("head").get(0)!!
  val stylesheet = (document.createElement("style") as HTMLStyleElement).apply {
    textContent = content
  }
  head.appendChild(stylesheet)
  return stylesheet
}
