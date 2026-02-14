/*
 * Copyright (C) 2025 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wasmo.domtester

import kotlin.math.ceil
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.files.Blob

/**
 * A image rendering of an HTML element.
 */
data class DomSnapshot(
  val images: List<Blob?>,
  val framedHtml: String,
)

class DomSnapshotter {
  suspend fun snapshot(
    element: HTMLElement,
    frame: Frame,
    scrolling: Boolean,
  ): DomSnapshot {
    val oldWidth = element.style.width
    val oldHeight = element.style.height
    element.style.width = "${frame.width}px"
    element.style.height = "${frame.height}px"
    try {
      val boundingClientRect = element.getBoundingClientRect()
      val elementWidth = ceil(boundingClientRect.width).toInt()
      val elementHeight = ceil(boundingClientRect.height).toInt()

      suspend fun captureImage(): Blob? {
        return html2canvas(
          element = element as HTMLElement,
          options = Options().apply {
            this.backgroundColor = null
            this.width = elementWidth
            this.height = elementHeight
            this.windowWidth = this.width
            this.windowHeight = this.height
            this.scale = frame.pixelRatio
          },
        ).await()?.encodeImage()
      }

      val images = buildList {
        if (!scrolling) {
          add(captureImage())
        } else {
          // Handle scrollable content.
          val scrollableElement = findScrollableElement(element)
            ?: throw IllegalStateException("No scrollable element found")

          // Calculate total number of pages needed.
          val totalPages =
            ceil(scrollableElement.scrollHeight.toDouble() / scrollableElement.clientHeight).toInt()

          for (page in 0 until totalPages) {
            scrollableElement.scrollTop = page * scrollableElement.clientHeight.toDouble()
            add(captureImage())
          }
        }
      }

      return DomSnapshot(
        images = images,
        framedHtml = document.documentElement!!.outerHTML,
      )
    } finally {
      element.style.width = oldWidth
      element.style.height = oldHeight
    }
  }

  private fun findScrollableElement(element: Element): HTMLElement? {
    val elements = element.getElementsByTagName("div")
    for (i in 0 until elements.length) {
      val div = elements.get(i) as? HTMLElement ?: continue
      val style = window.getComputedStyle(div)
      if (style.overflowY == "scroll" || style.overflowY == "auto") {
        if (div.scrollHeight > div.clientHeight) {
          return div
        }
      }
    }
    return null
  }
}
