package com.wasmo.accounts.passkeys

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.accounts.invite.InviteService
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.calls.CallDataService
import com.wasmo.db.passkeys.findPasskeyByPasskeyIdAndAccountId
import com.wasmo.db.passkeys.insertPasskey
import com.wasmo.framework.ArgumentUserException
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import com.wasmo.passkeys.PasskeyChecker
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.time.Clock
import org.postgresql.util.PSQLException
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@ClassKey(RegisterPasskeyRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class RegisterPasskeyRpc(
  private val clock: Clock,
  private val callDataService: CallDataService,
  private val client: Client,
  private val passkeyChecker: PasskeyChecker,
  private val wasmoDb: SqlDatabase,
  private val inviteService: InviteService,
) : RpcAction<RegisterPasskeyRequest, RegisterPasskeyResponse> {
  suspend fun register(
    request: RegisterPasskeyRequest,
  ): Response<RegisterPasskeyResponse> {
    val registerResult = passkeyChecker.register(request.registration)

    return wasmoDb.transaction {
      val accountId = client.getOrCreateAccountId()

      val existing = findPasskeyByPasskeyIdAndAccountId(registerResult.id, accountId)
      if (existing == null) {
        try {
          insertPasskey(
            createdAt = clock.now(),
            accountId = accountId,
            passkeyId = registerResult.id,
            aaguid = registerResult.aaguid,
            createdByUserAgent = client.userAgent,
            createdByIp = client.ip,
            registrationRecord = registerResult.record,
          )
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

  override suspend operator fun invoke(
    request: RegisterPasskeyRequest,
    url: Url,
  ) = register(request)
}
