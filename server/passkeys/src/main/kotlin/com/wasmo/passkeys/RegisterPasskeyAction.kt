package com.wasmo.passkeys


import com.wasmo.accounts.AccountStore
import com.wasmo.accounts.Client
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.db.PasskeyQueries
import com.wasmo.framework.Response
import kotlin.time.Clock

class RegisterPasskeyAction(
  private val clock: Clock,
  private val accountStoreFactory: AccountStore.Factory,
  private val client: Client,
  private val passkeyChecker: PasskeyChecker,
  private val passkeyQueries: PasskeyQueries,
) {
  fun register(
    request: RegisterPasskeyRequest,
  ): Response<RegisterPasskeyResponse> {
    val accountStore = accountStoreFactory.create(client)
    val registerResult = passkeyChecker.register(request.registration)

    return passkeyQueries.transactionWithResult(noEnclosing = true) {
      val accountId = client.getOrCreateAccountId()

      passkeyQueries.insertPasskey(
        created_at = clock.now(),
        account_id = accountId,
        passkey_id = registerResult.id,
        aaguid = registerResult.aaguid,
        created_by_user_agent = client.userAgent,
        created_by_ip = client.ip,
        registration_record = registerResult.record,
      ).value

      Response(
        body = RegisterPasskeyResponse(
          account = accountStore.snapshot(),
        ),
      )
    }
  }
}
