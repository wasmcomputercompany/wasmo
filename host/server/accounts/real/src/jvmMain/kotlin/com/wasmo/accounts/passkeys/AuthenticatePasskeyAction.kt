package com.wasmo.accounts.passkeys

import com.wasmo.accounts.Client
import com.wasmo.accounts.invite.InviteService
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.AuthenticatePasskeyResponse
import com.wasmo.app.db.WasmoDbService
import com.wasmo.calls.CallDataService
import com.wasmo.framework.BadRequestException
import com.wasmo.framework.Response
import com.wasmo.passkeys.PasskeyChecker

class AuthenticatePasskeyAction(
  private val client: Client,
  private val passkeyChecker: PasskeyChecker,
  private val passkeyLinker: PasskeyLinker,
  private val callDataService: CallDataService,
  private val wasmoDbService: WasmoDbService,
  private val inviteService: InviteService,
) {
  fun authenticate(
    request: AuthenticatePasskeyRequest,
  ): Response<AuthenticatePasskeyResponse> {
    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val passkey = wasmoDbService.passkeyQueries.findPasskeyByPasskeyId(request.authentication.id)
        .executeAsOneOrNull()
        ?: throw BadRequestException("no such passkey")

      try {
        passkeyChecker.authenticate(
          authentication = request.authentication,
          registrationRecord = passkey.registration_record,
        )
      } catch (_: Exception) {
        // TODO: log the exception
        throw BadRequestException("failed to authenticate passkey")
      }

      passkeyLinker.link(passkey)

      val inviteCode = request.inviteCode
      if (inviteCode != null) {
        inviteService.claim(client, inviteCode)
      }

      Response(
        body = AuthenticatePasskeyResponse(
          account = callDataService.accountSnapshot(),
        ),
      )
    }
  }
}
