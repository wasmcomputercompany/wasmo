package com.wasmo.accounts

import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AccountSnapshotResponse
import com.wasmo.calls.CallDataService
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@ClassKey(AccountSnapshotRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class AccountSnapshotRpc(
  private val callDataService: CallDataService,
  private val wasmoDb: SqlDatabase,
) : RpcAction<AccountSnapshotRequest, AccountSnapshotResponse> {
  suspend fun get(
    request: AccountSnapshotRequest,
  ): Response<AccountSnapshotResponse> {
    return wasmoDb.transaction {
      Response(
        body = AccountSnapshotResponse(
          account = callDataService.accountSnapshot(),
        ),
      )
    }
  }

  override suspend operator fun invoke(
    request: AccountSnapshotRequest,
    url: Url,
  ) = get(request)
}
