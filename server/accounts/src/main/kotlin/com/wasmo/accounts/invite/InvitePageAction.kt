package com.wasmo.accounts.invite

import com.wasmo.accounts.AccountStore
import com.wasmo.accounts.AppPageFactory
import com.wasmo.accounts.Client
import com.wasmo.app.db.WasmoDbService
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody

class InvitePageAction(
  private val client: Client,
  private val accountStoreFactory: AccountStore.Factory,
  private val appPageFactory: AppPageFactory,
  private val wasmoDbService: WasmoDbService,
) {
  fun invite(code: String): Response<ResponseBody> {
    // TODO: associate code with the current user's session?
    val accountStore = accountStoreFactory.create(client)
    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val accountSnapshot = accountStore.snapshot()
      appPageFactory.create(accountSnapshot)
    }
  }
}
