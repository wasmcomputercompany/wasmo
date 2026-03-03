package com.wasmo.accounts

import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AccountSnapshotResponse
import com.wasmo.app.db.WasmoDbService
import com.wasmo.calls.CallDataService
import com.wasmo.framework.Response

class AccountSnapshotAction(
  private val callDataService: CallDataService,
  private val wasmoDbService: WasmoDbService,
) {
  fun get(
    request: AccountSnapshotRequest,
  ): Response<AccountSnapshotResponse> {
    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      Response(
        body = AccountSnapshotResponse(
          account = callDataService.accountSnapshot(),
        ),
      )
    }
  }
}
