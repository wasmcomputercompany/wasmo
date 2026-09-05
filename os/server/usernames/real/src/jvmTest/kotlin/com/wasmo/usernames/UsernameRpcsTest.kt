package com.wasmo.usernames

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.wasmo.api.CreateUsernameDecision
import com.wasmo.api.LinkUsernameDecision
import com.wasmo.api.SignOutRequest
import com.wasmo.api.SignOutResponse
import com.wasmo.api.routes.SignInRoute
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

    val createResponse = client.createUsername(username = username, password = "")
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

    val linkResponse = client.linkUsername(username = username, password = "")
    assertThat(linkResponse.body.account?.username?.username)
      .isEqualTo(username)
  }

  @Test
  fun `create and then link username RPCs from two clients`() = runTest {
    val client = tester.newClient()

    val username = client.createNewUsername()

    val createResponse = client.createUsername(username = username, password = "")
    val responseAccount = createResponse.body.account
    assertThat(responseAccount?.username?.username)
      .isEqualTo(username)

    val client2 = tester.newClient()
    val linkResponse = client2.linkUsername(username = username, password = "")
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

  @Test
  fun `create and then link username enforces password`() = runTest {
    val client = tester.newClient()
    val username = client.createNewUsername()
    val createResponse = client.createUsername(username = username, password = "password")

    val linkNoPasswordResponse = client.linkUsername(username = username, password = "")
    val linkWrongPasswordResponse = client.linkUsername(username = username, password = "wrong password")
    val linkRightPasswordResponse = client.linkUsername(username = username, password = "password")

    assertThat(createResponse.body.decision)
      .isEqualTo(CreateUsernameDecision.Success)

    val linkDecisions = listOf(linkNoPasswordResponse, linkWrongPasswordResponse, linkRightPasswordResponse).associateWith { it.body.decision }

    assertThat(linkDecisions).isEqualTo(mapOf(
        linkNoPasswordResponse to LinkUsernameDecision.PasswordAuthenticationFailed,
        linkNoPasswordResponse to LinkUsernameDecision.PasswordAuthenticationFailed,
        linkRightPasswordResponse to LinkUsernameDecision.Success,
    ))
  }

  @Test
  fun `create existing username fails`() = runTest {
    val client = tester.newClient()
    val username = client.createNewUsername()
    val createResponseA = client.createUsername(username = username, password = "password")
    val createResponseB = client.createUsername(username = username, password = "password")
    val createResponseC = client.createUsername(username = username, password = "wrong password")

    val decisions = listOf(createResponseA, createResponseB, createResponseC).map { it.body.decision }

    assertThat(decisions).isEqualTo(listOf(
      CreateUsernameDecision.Success,
      CreateUsernameDecision.UsernameTaken,
      CreateUsernameDecision.UsernameTaken,
    ))
  }

  @Test
  fun `link nonexistent username fails`() = runTest {
    val client = tester.newClient()
    val username = client.createNewUsername()
    val decision = client.linkUsername(username = username, password = "").body.decision
    assertThat(decision).isEqualTo(LinkUsernameDecision.UsernameNotFound)
  }

  @Test
  fun `when usernames are present then signInSnapshot reports SignInConfigs to signed-out clients`() = runTest {
    val client = tester.newClient()
    val usernameWithPassword = client.createNewUsername()

    client.createUsername(username = usernameWithPassword, password = "password")
    val usernameWithoutPassword = client.createNewUsername()
    // As of 2026-08, we have to sign-out because creating a username while signed-in as a usernam
    // (crashes at the SQL level because a UNIQUE constraint is violated trying to attach the second
    // username to the signed-in account).
    client.call().signOut(SignOutRequest)

    client.createUsername(username = usernameWithoutPassword, password = "")
    client.call().signOut(SignOutRequest)
    val signInSnapshot = client.call().osPage(SignInRoute).signInSnapshot!!
    val usernameOptions  = signInSnapshot.usernameOptions

    assertThat(usernameOptions.keys).containsAtLeast(usernameWithPassword, usernameWithoutPassword)
    assertThat(usernameOptions[usernameWithoutPassword]!!.isPasswordRequired).isFalse()
    assertThat(usernameOptions[usernameWithPassword]!!.isPasswordRequired).isTrue()
  }
}

