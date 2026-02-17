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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.renderComposableInBody
import org.w3c.dom.HTMLBodyElement
import org.w3c.dom.HTMLElement
import org.w3c.files.Blob

class SnapshotTester(
  private val stylesheetsUrls: List<String> = listOf(),
) : CoroutineTestInterceptor {
  private val snapshotStore: SnapshotStore = SnapshotStore()
  private val domSnapshotter: DomSnapshotter = DomSnapshotter()
  private val imageDiffer: ImageDiffer = ImageDiffer()
  private var testFunction: CoroutineTestFunction? = null
  private var snapshotCount = 0

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    this.testFunction = testFunction
    val stylesheetLinkElements = (document.documentElement as HTMLElement)
      .addStylesheets(stylesheetsUrls)
    try {
      testFunction()
    } finally {
      this.testFunction = null
      for (element in stylesheetLinkElements) {
        element.remove()
      }
    }
  }

  suspend fun snapshot(
    element: HTMLElement,
    frame: Frame,
    name: String? = null,
    scrolling: Boolean = false,
  ) {
    check(snapshotCount++ == 0 || name != null) {
      "a name must be specified when taking multiple snapshots"
    }

    val pathPrefix = pathPrefix(name)
    val htmlPath = "$pathPrefix.actual.html"

    val domSnapshot = domSnapshotter.snapshot(element, frame, scrolling)
    val images = domSnapshot.images
    val html = domSnapshot.htmlPage(
      title = testFunction.toString(),
      stylesheetsUrls = stylesheetsUrls,
      baseHref = "http://localhost:8080/",
    )

    if (images.any { it == null }) {
      snapshotStore.put(htmlPath, Blob(arrayOf(html)), writeToBuildDir = true)
      throw SnapshotMismatchException("html2canvas returned null for $pathPrefix.png")
    }

    var createdNewSnapshot = false

    for ((index, image) in images.withIndex()) {
      snapshotStore.put(htmlPath, Blob(arrayOf(html)), writeToBuildDir = true)
      val pngPath = when (index) {
        0 -> "$pathPrefix.png"
        else -> "${pathPrefix}_$index.png"
      }

      val existing = snapshotStore.getBlob(pngPath)
      if (existing == null) {
        snapshotStore.put(pngPath, image!!)
        createdNewSnapshot = true
        continue
      }

      val diffResult = imageDiffer.compare(existing, image!!)
      if (diffResult.isDifferent) {
        // Save the delta image and wrapped HTML so the developer can see what's different.
        snapshotStore.put("$pathPrefix.diff.png", diffResult.deltaImage!!, writeToBuildDir = true)

        throw SnapshotMismatchException(
          "Current snapshot does not match the existing file $pngPath " +
            "(${diffResult.percentDifference}% different, ${diffResult.numDifferentPixels} pixels)",
        )
      }
    }

    if (createdNewSnapshot) {
      throw SnapshotMismatchException("Created new snapshot file $pathPrefix.png")
    }
  }

  suspend fun snapshot(
    frame: Frame = Frame.Iphone14,
    name: String? = null,
    scrolling: Boolean = false,
    content: @Composable DOMScope<HTMLBodyElement>.() -> Unit,
  ): Composition {
    val composition = renderComposableInBody(content)
    snapshot(
      element = document.body!!,
      frame = frame,
      name = name,
      scrolling = scrolling,
    )
    return composition
  }

  private fun pathPrefix(name: String?) = buildString {
    val testFunction = testFunction
      ?: error("unexpected call to snapshot(): is the interceptor applied?")
    append(testFunction.packageName)
    append("/")
    append(testFunction.className)
    append(".")
    append(testFunction.functionName)
    if (name != null) {
      append("-")
      append(name)
    }
  }
}
