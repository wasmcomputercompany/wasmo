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
@file:OptIn(ExperimentalJsExport::class)

package com.wasmo.domtester

import app.cash.burst.Burst
import app.cash.burst.InterceptTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.browser.document
import kotlinx.coroutines.test.runTest
import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import kotlinx.dom.clear

/**
 * This isn't a proper unit test for [SnapshotTester], it's just a sample.
 */
@Burst
internal class SnapshotTesterSampleTest {
  @InterceptTest
  private val snapshotTester = SnapshotTester()

  @AfterTest
  fun afterTest() {
    document.body?.clear()
  }

  @Test
  fun happyPath() = runTest {
    val helloWorld = document.body!!.apply {
      appendElement("h1") {
        appendText("hello world")
      }
    }

    snapshotTester.snapshot(helloWorld, Frame.Iphone14)
  }

  @Test
  fun mismatchedSnapshot() = runTest {
    val body = document.body!!
    body.apply {
      appendElement("h1") {
        appendText("hello world")
      }
    }
    snapshotTester.snapshot(body, Frame.Iphone14, "mismatch")

    body.apply {
      clear()
      appendElement("h2") {
        appendText("hello world")
      }
    }
    assertFailsWith<SnapshotMismatchException> {
      snapshotTester.snapshot(body, Frame.Iphone14, "mismatch")
    }
  }
}
