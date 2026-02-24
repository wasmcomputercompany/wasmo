package com.wasmo.accounts.passkeys


import com.wasmo.accounts.AccountStore
import com.wasmo.accounts.Client
import com.wasmo.accounts.invite.InviteService
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.app.db.WasmoDbService
import com.wasmo.framework.Response
import com.wasmo.passkeys.PasskeyChecker
import kotlin.time.Clock

class RegisterPasskeyAction(
  private val clock: Clock,
  private val accountStoreFactory: AccountStore.Factory,
  private val client: Client,
  private val passkeyChecker: PasskeyChecker,
  private val wasmoDbService: WasmoDbService,
  private val inviteService: InviteService,
) {
  fun register(
    request: RegisterPasskeyRequest,
  ): Response<RegisterPasskeyResponse> {
    val accountStore = accountStoreFactory.create(client)
    val registerResult = passkeyChecker.register(request.registration)

    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val accountId = client.getOrCreateAccountId()

      wasmoDbService.passkeyQueries.insertPasskey(
        created_at = clock.now(),
        account_id = accountId,
        passkey_id = registerResult.id,
        aaguid = registerResult.aaguid,
        created_by_user_agent = client.userAgent,
        created_by_ip = client.ip,
        registration_record = registerResult.record,
      ).value

      val inviteCode = request.inviteCode
      if (inviteCode != null) {
        inviteService.claim(client, inviteCode)
      }

      Response(
        body = RegisterPasskeyResponse(
          account = accountStore.snapshot(),
        ),
      )
    }
  }
}
