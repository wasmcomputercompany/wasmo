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

import app.cash.burst.coroutines.CoroutineTestFunction
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.browser.document
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import kotlinx.dom.clear
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text

/**
 * Note that this doesn't apply [SnapshotTester] with Burst. Instead, it sets up the interceptor
 * manually in order to confirm exceptions are propagated properly.
 */
internal class SnapshotTesterSampleTest {
  private val snapshotTester = SnapshotTester()

  @AfterTest
  fun afterTest() {
    document.body?.clear()
  }

  @Test
  fun happyPath() = runTest {
    interceptTest("happyPath") {
      val helloWorld = document.body!!.apply {
        appendElement("h1") {
          appendText("hello world")
        }
      }

      snapshotTester.snapshot(helloWorld, Frame.Iphone14)
    }
  }

  @Test
  fun compose() = runTest {
    interceptTest("compose") {
      snapshotTester.snapshot {
        H1 {
          Text("hello compose")
        }
      }
    }
  }

  @Test
  fun mismatchedSnapshot() = runTest {
    interceptTest("mismatchedSnapshot") {
      val body = document.body!!
      body.apply {
        appendElement("h1") {
          appendText("hello world")
        }
      }
      snapshotTester.snapshot(body, Frame.Iphone14, "mismatch")
    }

    assertFailsWith<SnapshotMismatchException> {
      interceptTest("mismatchedSnapshot") {
        val body = document.body!!
        body.apply {
          clear()
          appendElement("h2") {
            appendText("hello world")
          }
        }
        snapshotTester.snapshot(body, Frame.Iphone14, "mismatch")
      }
    }
  }

  private suspend fun TestScope.interceptTest(functionName: String, body: suspend () -> Unit) {
    snapshotTester.intercept(
      object : CoroutineTestFunction(
        scope = this,
        packageName = "com.wasmo.domtester",
        className = "SnapshotTesterSampleTest",
        functionName = functionName,
      ) {
        override suspend fun invoke() {
          body()
        }
      },
    )
  }
}
