package com.wasmo.client.app

import com.wasmo.domtester.Frame
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.browser.document
import kotlinx.coroutines.test.runTest
import kotlinx.dom.addClass
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.Document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.get

class HomeTest {
  private val snapshotTester = SnapshotTester(
    path = "com.wasmo.client.app/HomeTest",
  )

  @Test
  fun happyPath() = runTest {
    document.addStylesheet("https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap")
    document.addStylesheet("/assets/Wasmo.css")

    val body = document.createElement("div").apply {
      this as HTMLDivElement
      addClass("root")
      style.width = "100%"
      style.height = "100%"
    }
    renderComposable(
      root = body,
    ) {
      Home(
        childStyle = {},
      )
    }
    snapshotTester.snapshot(
      element = body,
      frame = Frame.Iphone14,
    )
  }
}

fun Document.addStylesheet(href: String) {
  getElementsByTagName("head").get(0)!!.apply {
    appendChild(
      createElement("link").apply {
        setAttribute("href", href)
        setAttribute("rel", "stylesheet")
      },
    )
  }
}
