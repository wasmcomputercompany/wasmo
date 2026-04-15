package com.wasmo.accounts

import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AccountSnapshotResponse
import com.wasmo.app.db.transactionWithResult
import com.wasmo.calls.CallDataService
import com.wasmo.framework.Response
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.sql.SqlDatabase

@Inject
@SingleIn(CallScope::class)
class AccountSnapshotAction(
  private val callDataService: CallDataService,
  private val wasmoDb: SqlDatabase,
) {
  suspend fun get(
    request: AccountSnapshotRequest,
  ): Response<AccountSnapshotResponse> {
    return wasmoDb.transactionWithResult(noEnclosing = true) {
      Response(
        body = AccountSnapshotResponse(
          account = callDataService.accountSnapshot(),
        ),
      )
    }
  }
}
