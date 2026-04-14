package com.wasmo.accounts.passkeys

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.accounts.invite.InviteService
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.calls.CallDataService
import com.wasmo.app.db.WasmoDb
import com.wasmo.framework.ArgumentUserException
import com.wasmo.framework.Response
import com.wasmo.passkeys.PasskeyChecker
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import org.postgresql.util.PSQLException

@Inject
@SingleIn(CallScope::class)
class RegisterPasskeyAction(
  private val clock: Clock,
  private val callDataService: CallDataService,
  private val client: Client,
  private val passkeyChecker: PasskeyChecker,
  private val wasmoDb: WasmoDb,
  private val inviteService: InviteService,
) {
  fun register(
    request: RegisterPasskeyRequest,
  ): Response<RegisterPasskeyResponse> {
    val registerResult = passkeyChecker.register(request.registration)

    return wasmoDb.transactionWithResult(noEnclosing = true) {
      val accountId = client.getOrCreateAccountId()

      val existing = wasmoDb.passkeyQueries
        .findPasskeyByPasskeyIdAndAccountId(registerResult.id, accountId)
        .executeAsOneOrNull()
      if (existing == null) {
        try {
          wasmoDb.passkeyQueries.insertPasskey(
            created_at = clock.now(),
            account_id = accountId,
            passkey_id = registerResult.id,
            aaguid = registerResult.aaguid,
            created_by_user_agent = client.userAgent,
            created_by_ip = client.ip,
            registration_record = registerResult.record,
          ).value
        } catch (_: PSQLException) {
          throw ArgumentUserException("already registered")
        }
      }

      val inviteCode = request.inviteCode
      if (inviteCode != null) {
        inviteService.claim(client, inviteCode)
      }

      Response(
        body = RegisterPasskeyResponse(
          account = callDataService.accountSnapshot(),
        ),
      )
    }
  }
}
