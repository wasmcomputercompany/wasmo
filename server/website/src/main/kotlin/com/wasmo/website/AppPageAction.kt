package com.wasmo.website

import com.wasmo.accounts.AccountStore
import com.wasmo.accounts.Client
import com.wasmo.app.db.WasmoDbService

/**
 * We serve the same page to most routes.
 */
class AppPageAction(
  private val client: Client,
  private val accountStoreFactory: AccountStore.Factory,
  private val appPageFactory: ServerAppPage.Factory,
  private val wasmoDbService: WasmoDbService,
) {
  fun get(): ServerAppPage {
    val accountStore = accountStoreFactory.create(client)
    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val accountSnapshot = accountStore.snapshot()
      appPageFactory.create(accountSnapshot)
    }
  }
}
