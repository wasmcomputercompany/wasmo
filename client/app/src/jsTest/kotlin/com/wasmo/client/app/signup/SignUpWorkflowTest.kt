package com.wasmo.client.app.signup

import app.cash.burst.InterceptTest
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SignUpWorkflowTest {
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
      SignUpIntro { _ ->
      }
    }
  }

  @Test
  fun credentials() = runTest {
    snapshotTester.snapshot {
      SignUpCredentials { _ ->
      }
    }
  }

  @Test
  fun payment() = runTest {
    snapshotTester.snapshot {
      SignUpPayment { _ ->
      }
    }
  }

  @Test
  fun createWasmo() = runTest {
    snapshotTester.snapshot {
      SignUpCreateWasmo { _ ->
      }
    }
  }

  @Test
  fun challengeCode() = runTest {
    snapshotTester.snapshot {
      SignUpChallengeCode { _ ->
      }
    }
  }
}
