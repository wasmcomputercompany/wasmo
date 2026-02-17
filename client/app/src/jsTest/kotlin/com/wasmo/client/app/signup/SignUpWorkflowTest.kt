package com.wasmo.client.app.signup

import app.cash.burst.InterceptTest
import com.wasmo.client.app.FormScreen
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
      FormScreen {
        SignUpToolbar()
        SignUpIntro { _ ->
        }
      }
    }
  }

  @Test
  fun credentials() = runTest {
    snapshotTester.snapshot {
      FormScreen {
        SignUpToolbar()
        SignUpCredentials { _ ->
        }
      }
    }
  }

  @Test
  fun payment() = runTest {
    snapshotTester.snapshot {
      FormScreen {
        SignUpToolbar()
        SignUpPayment { _ ->
        }
      }
    }
  }

  @Test
  fun createWasmo() = runTest {
    snapshotTester.snapshot {
      FormScreen {
        SignUpToolbar()
        SignUpCreateWasmo { _ ->
        }
      }
    }
  }

  @Test
  fun challengeCode() = runTest {
    snapshotTester.snapshot {
      FormScreen {
        SignUpToolbar()
        SignUpChallengeCode { _ ->
        }
      }
    }
  }
}
