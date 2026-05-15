package com.wasmo.usernames

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.wasmo.api.SignOutRequest
import com.wasmo.api.SignOutResponse
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class UsernameRpcsTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun `create and then link username RPCs from one client`() = runTest {
    val client = tester.newClient()

    val username = client.createNewUsername()

    val createResponse = client.createUsername(username = username)
    val responseAccount = createResponse.body.account
    assertThat(responseAccount?.username?.username)
      .isEqualTo(username)

    val accountSnapshotResponse1 = client.accountSnapshot()
    assertThat(accountSnapshotResponse1.username?.username)
      .isEqualTo(username)

    val signOutResponse = client.call().signOut(SignOutRequest)
    assertThat(signOutResponse.body).isEqualTo(SignOutResponse)

    val accountSnapshotResponse2 = client.accountSnapshot()
    assertThat(accountSnapshotResponse2.username?.username)
      .isNull()

    val linkResponse = client.linkUsername(username = username)
    assertThat(linkResponse.body.account?.username?.username)
      .isEqualTo(username)
  }

  @Test
  fun `create and then link username RPCs from two clients`() = runTest {
    val client = tester.newClient()

    val username = client.createNewUsername()

    val createResponse = client.createUsername(username = username)
    val responseAccount = createResponse.body.account
    assertThat(responseAccount?.username?.username)
      .isEqualTo(username)

    val client2 = tester.newClient()
    val linkResponse = client2.linkUsername(username = username)
    assertThat(linkResponse.body.account?.username?.username)
      .isEqualTo(username)

    // both clients are now simultaneously linked to the same username
    val accountSnapshotResponse1 = client.accountSnapshot()
    assertThat(accountSnapshotResponse1.username?.username)
      .isEqualTo(username)
    val accountSnapshotResponse2 = client2.accountSnapshot()
    assertThat(accountSnapshotResponse2.username?.username)
      .isNotNull()

    assertThat(accountSnapshotResponse1.isSignedIn)
      .isTrue()
    assertThat(accountSnapshotResponse2.isSignedIn)
      .isTrue()

    val signOutResponse = client2.call().signOut(SignOutRequest)
    assertThat(signOutResponse.body).isEqualTo(SignOutResponse)
  }

  // TODO: Check that sign-in page contains signInSnapshot;
  // This is currently only the case when Assume.assumeTrue(findUsernamesThatCanSignIn().isNotEmpty())
}

