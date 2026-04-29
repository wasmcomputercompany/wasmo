package com.wasmo.accounts

import com.wasmo.api.SignOutRequest
import com.wasmo.api.SignOutResponse
import com.wasmo.framework.Response
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@SingleIn(CallScope::class)
class SignOutRpc(
  private val client: Client,
  private val wasmoDb: SqlDatabase,
) {
  suspend fun signOut(request: SignOutRequest): Response<SignOutResponse> {
    return wasmoDb.transaction {
      client.signOut()
      Response(
        body = SignOutResponse,
      )
    }
  }
}
