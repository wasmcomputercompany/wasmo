package com.wasmo.accounts.passkeys

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.accounts.invite.InviteService
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.AuthenticatePasskeyResponse
import com.wasmo.calls.CallDataService
import com.wasmo.app.db.WasmoDb
import com.wasmo.framework.ArgumentUserException
import com.wasmo.framework.Response
import com.wasmo.passkeys.PasskeyChecker
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(CallScope::class)
class AuthenticatePasskeyAction(
  private val client: Client,
  private val passkeyChecker: PasskeyChecker,
  private val passkeyLinker: PasskeyLinker,
  private val callDataService: CallDataService,
  private val wasmoDb: WasmoDb,
  private val inviteService: InviteService,
) {
  fun authenticate(
    request: AuthenticatePasskeyRequest,
  ): Response<AuthenticatePasskeyResponse> {
    return wasmoDb.transactionWithResult(noEnclosing = true) {
      val passkey = wasmoDb.passkeyQueries.findPasskeyByPasskeyId(request.authentication.id)
        .executeAsOneOrNull()
        ?: throw ArgumentUserException("no such passkey")

      try {
        passkeyChecker.authenticate(
          authentication = request.authentication,
          registrationRecord = passkey.registration_record,
        )
      } catch (_: Exception) {
        // TODO: log the exception
        throw ArgumentUserException("failed to authenticate passkey")
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
