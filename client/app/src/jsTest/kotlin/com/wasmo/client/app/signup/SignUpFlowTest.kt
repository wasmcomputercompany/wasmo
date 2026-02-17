package com.wasmo.client.app.signup

import app.cash.burst.InterceptTest
import com.wasmo.compose.ChildStyle
import com.wasmo.domtester.Frame
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.browser.document
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.web.renderComposableInBody

class SignUpFlowTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
      "/assets/Wasmo.css",
    ),
  )

  @Test
  fun intro() = runTest {
    renderComposableInBody {
      SignUpIntro(ChildStyle {})
    }
    snapshotTester.snapshot(
      element = document.body!!,
      frame = Frame.Iphone14,
    )
  }

  @Test
  fun credentials() = runTest {
    renderComposableInBody {
      SignUpCredentials(ChildStyle {})
    }
    snapshotTester.snapshot(
      element = document.body!!,
      frame = Frame.Iphone14,
    )
  }

  @Test
  fun payment() = runTest {
    renderComposableInBody {
      SignUpPayment(ChildStyle {})
    }
    snapshotTester.snapshot(
      element = document.body!!,
      frame = Frame.Iphone14,
    )
  }

  @Test
  fun createWasmo() = runTest {
    renderComposableInBody {
      SignUpCreateWasmo(ChildStyle {})
    }
    snapshotTester.snapshot(
      element = document.body!!,
      frame = Frame.Iphone14,
    )
  }

  @Test
  fun challengeCode() = runTest {
    renderComposableInBody {
      SignUpChallengeCode(ChildStyle {})
    }
    snapshotTester.snapshot(
      element = document.body!!,
      frame = Frame.Iphone14,
    )
  }
}
