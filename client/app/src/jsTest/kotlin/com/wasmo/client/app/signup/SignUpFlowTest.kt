package com.wasmo.client.app.signup

import app.cash.burst.InterceptTest
import com.wasmo.compose.ChildStyle
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

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
    snapshotTester.snapshot {
      SignUpIntro(ChildStyle {}) { _ ->
      }
    }
  }

  @Test
  fun credentials() = runTest {
    snapshotTester.snapshot {
      SignUpCredentials(ChildStyle {}) { _ ->
      }
    }
  }

  @Test
  fun payment() = runTest {
    snapshotTester.snapshot {
      SignUpPayment(ChildStyle {}) { _ ->
      }
    }
  }

  @Test
  fun createWasmo() = runTest {
    snapshotTester.snapshot {
      SignUpCreateWasmo(ChildStyle {}) { _ ->
      }
    }
  }

  @Test
  fun challengeCode() = runTest {
    snapshotTester.snapshot {
      SignUpChallengeCode(ChildStyle {}) { _ ->
      }
    }
  }
}
