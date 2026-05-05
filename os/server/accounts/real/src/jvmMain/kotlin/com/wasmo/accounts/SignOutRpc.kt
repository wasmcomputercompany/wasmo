package com.wasmo.accounts

import com.wasmo.api.SignOutRequest
import com.wasmo.api.SignOutResponse
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import com.wasmo.framework.UserAgent
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@ClassKey(SignOutRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class SignOutRpc(
  private val client: Client,
  private val wasmoDb: SqlDatabase,
) : RpcAction<SignOutRequest, SignOutResponse> {
  suspend fun signOut(request: SignOutRequest): Response<SignOutResponse> {
    return wasmoDb.transaction {
      client.signOut()
      Response(
        body = SignOutResponse,
      )
    }
  }

  override suspend fun invoke(
    userAgent: UserAgent,
    request: SignOutRequest,
    url: Url,
  ) = signOut(request)
}
