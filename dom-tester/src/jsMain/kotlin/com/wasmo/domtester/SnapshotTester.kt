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

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import org.w3c.dom.HTMLElement
import org.w3c.files.Blob

class SnapshotTester @PublishedApi internal constructor(
  private val snapshotStore: SnapshotStore = SnapshotStore(),
  private val domSnapshotter: DomSnapshotter = DomSnapshotter(),
  private val imageDiffer: ImageDiffer = ImageDiffer(),
) : CoroutineTestInterceptor {
  private var testFunction: CoroutineTestFunction? = null
  private var snapshotCount = 0

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    this.testFunction = testFunction
    try {
      testFunction()
    } finally {
      this.testFunction = null
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

    val (images, html) = domSnapshotter.snapshot(element, frame, scrolling)

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

  companion object Companion {
    operator fun invoke() = SnapshotTester()
  }
}
