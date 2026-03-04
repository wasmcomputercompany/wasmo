package com.wasmo.accounts.invite

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.decodeUrl
import com.wasmo.framework.BadRequestException
import com.wasmo.framework.NotFoundException
import com.wasmo.testing.WasmoServiceTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class InvitesTest {
  lateinit var tester: WasmoServiceTester

  @BeforeTest
  fun setUp() {
    tester = WasmoServiceTester.start()
  }

  @AfterTest
  fun tearDown() {
    tester.close()
  }

  @Test
  fun unknownCode404s() {
    val client = tester.newClient()
    assertFailsWith<NotFoundException> {
      client.call().hostPage(InviteRoute(code = "12345"))
    }
  }

  @Test
  fun receiveAndClaimInviteWithPasskeyRegistration() {
    val createdByClient = tester.newClient()
    val createInviteResponse = createdByClient.call().createInvite(CreateInviteRequest)

    val inviteUrl = createInviteResponse.body.inviteUrl.decodeUrl()
    val inviteRoute = createdByClient.call().routeCodec().decode(inviteUrl) as InviteRoute

    val claimedByClient = tester.newClient()

    val appPage = claimedByClient.call().hostPage(inviteRoute)
    assertThat(appPage.inviteTicket?.code).isEqualTo(inviteRoute.code)
    assertThat(appPage.inviteTicket?.claimed).isEqualTo(false)

    val accountSnapshotResponse = claimedByClient.call().accountSnapshot(AccountSnapshotRequest)
    assertThat(accountSnapshotResponse.body.account.hasInvite).isFalse()

    val passkey = tester.newPasskey()
    val registerResponse = claimedByClient.register(passkey, inviteRoute.code)

    assertThat(registerResponse.body.account.hasInvite).isTrue()
  }

  @Test
  fun claimInviteIsIdempotent() {
    val createdByClient = tester.newClient()
    val createInviteResponse = createdByClient.call().createInvite(CreateInviteRequest)

    val inviteUrl = createInviteResponse.body.inviteUrl.decodeUrl()
    val inviteRoute = createdByClient.call().routeCodec().decode(inviteUrl) as InviteRoute

    val claimedByClient = tester.newClient()
    val passkey = tester.newPasskey()
    claimedByClient.register(passkey, inviteRoute.code)
    claimedByClient.register(passkey, inviteRoute.code)
    claimedByClient.call().authenticate(passkey, inviteRoute.code)
    claimedByClient.call().authenticate(passkey, inviteRoute.code)
  }

  /** We have to be careful that our database 'WHERE' clauses do case-sensitive search on tokens. */
  @Test
  fun claimedTokensAreCaseSensitive() {
    val createdByClient = tester.newClient()
    val createInviteResponse = createdByClient.call().createInvite(CreateInviteRequest)

    val inviteUrl = createInviteResponse.body.inviteUrl.decodeUrl()
    val inviteRoute = createdByClient.call().routeCodec().decode(inviteUrl) as InviteRoute

    val claimedByClient = tester.newClient()

    val passkey = tester.newPasskey()
    assertFailsWith<BadRequestException> {
      claimedByClient.register(passkey, inviteRoute.code.uppercase())
    }
  }

  @Test
  fun receiveAndClaimInviteWithPasskeyAuthentication() {
    val passkey = tester.newPasskey()

    val passkeyRegisterClient = tester.newClient()
    passkeyRegisterClient.register(passkey)

    val createdByClient = tester.newClient()
    val createInviteResponse = createdByClient.call().createInvite(CreateInviteRequest)

    val inviteUrl = createInviteResponse.body.inviteUrl.decodeUrl()
    val inviteRoute = createdByClient.call().routeCodec().decode(inviteUrl) as InviteRoute

    val claimedByClient = tester.newClient()
    val accountSnapshotResponse = claimedByClient.call().accountSnapshot(AccountSnapshotRequest)
    assertThat(accountSnapshotResponse.body.account.hasInvite).isFalse()

    val authenticateResponse = claimedByClient.call().authenticate(passkey, inviteRoute.code)
    assertThat(authenticateResponse.body.account.hasInvite).isTrue()
  }
}
