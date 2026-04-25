package com.wasmo.emails

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import com.wasmo.api.SignOutRequest
import com.wasmo.api.SignOutResponse
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SignInSignOutTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun `sign up sign out rpc`() = runTest {
    val client = tester.newClient()

    val confirmResponse = client.linkAndConfirmEmailAddress("jesse@example.com")
    assertThat(confirmResponse.body.account?.emailAddresses)
      .isNotNull()
      .isNotEmpty()

    val accountSnapshotResponse1 = client.accountSnapshot()
    assertThat(accountSnapshotResponse1.emailAddresses)
      .isNotNull()
      .isNotEmpty()

    val signOutResponse = client.call().signOut(SignOutRequest)
    assertThat(signOutResponse.body).isEqualTo(SignOutResponse)

    val accountSnapshotResponse2 = client.accountSnapshot()
    assertThat(accountSnapshotResponse2.emailAddresses)
      .isNotNull()
      .isEmpty()
  }

  @Test
  fun `sign up sign out page`() = runTest {
    val client = tester.newClient()

    val confirmResponse = client.linkAndConfirmEmailAddress("jesse@example.com")
    assertThat(confirmResponse.body.account?.emailAddresses)
      .isNotNull()
      .isNotEmpty()

    val accountSnapshotResponse1 = client.accountSnapshot()
    assertThat(accountSnapshotResponse1.emailAddresses)
      .isNotNull()
      .isNotEmpty()

    val signOutResponse = client.call().signOutPage()
    assertThat(signOutResponse.header("Location"))
      .isEqualTo("https://wasmo.com/")

    val accountSnapshotResponse2 = client.accountSnapshot()
    assertThat(accountSnapshotResponse2.emailAddresses)
      .isNotNull()
      .isEmpty()
  }

  @Test
  fun `sign in sign out rpc`() = runTest {
    // Sign up with client1.
    val client1 = tester.newClient()
    client1.linkAndConfirmEmailAddress("jesse@example.com")

    // Sign in with client2.
    val client2 = tester.newClient()
    client2.linkAndConfirmEmailAddress("jesse@example.com")

    val accountSnapshotResponse1 = client2.accountSnapshot()
    assertThat(accountSnapshotResponse1.emailAddresses)
      .isNotNull()
      .isNotEmpty()

    val signOutResponse = client2.call().signOut(SignOutRequest)
    assertThat(signOutResponse.body).isEqualTo(SignOutResponse)

    val accountSnapshotResponse2 = client2.accountSnapshot()
    assertThat(accountSnapshotResponse2.emailAddresses)
      .isNotNull()
      .isEmpty()
  }
}
