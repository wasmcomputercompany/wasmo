package com.wasmo.passkeys

import com.wasmo.accounts.AccountStore
import com.wasmo.accounts.Client
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.AuthenticatePasskeyResponse
import com.wasmo.db.PasskeyQueries
import com.wasmo.framework.BadRequestException
import com.wasmo.framework.Response

class AuthenticatePasskeyAction(
  private val client: Client,
  private val passkeyChecker: PasskeyChecker,
  private val passkeyLinkerFactory: PasskeyLinker.Factory,
  private val accountStoreFactory: AccountStore.Factory,
  private val passkeyQueries: PasskeyQueries,
) {
  fun authenticate(
    request: AuthenticatePasskeyRequest,
  ): Response<AuthenticatePasskeyResponse> {
    return passkeyQueries.transactionWithResult(noEnclosing = true) {
      val passkey = passkeyQueries.findPasskeyByPasskeyId(request.authentication.id)
        .executeAsOneOrNull()
        ?: throw BadRequestException("no such passkey")

      try {
        passkeyChecker.authenticate(
          request.authentication,
          passkey.registration_record,
        )
      } catch (_: Exception) {
        // TODO: log the exception
        throw BadRequestException("failed to authenticate passkey")
      }

      passkeyLinkerFactory.create(client).link(passkey)

      val accountStore = accountStoreFactory.create(client)
      Response(
        body = AuthenticatePasskeyResponse(
          account = accountStore.snapshot(),
        ),
      )
    }
  }
}
