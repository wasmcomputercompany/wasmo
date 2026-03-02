package com.wasmo.website

import com.wasmo.accounts.AccountStore
import com.wasmo.accounts.Client
import com.wasmo.api.ComputerListItem
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSlug
import com.wasmo.app.db.WasmoDbService

/**
 * We serve the same page to most routes.
 */
class HostPageAction(
  private val client: Client,
  private val accountStoreFactory: AccountStore.Factory,
  private val hostPageFactory: ServerHostPage.Factory,
  private val wasmoDbService: WasmoDbService,
) {
  fun get(): ServerHostPage {
    val accountStore = accountStoreFactory.create(client)

    // TODO: only include this if the requested route is to the ComputerList page.
    val computerListSnapshot = ComputerListSnapshot(
      items = listOf(
        ComputerListItem(
          slug = ComputerSlug("jesse99"),
        ),
        ComputerListItem(
          slug = ComputerSlug("rounds"),
        ),
      ),
    )

    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val accountSnapshot = accountStore.snapshot()
      hostPageFactory.create(
        accountSnapshot = accountSnapshot,
        computerListSnapshot = computerListSnapshot,
      )
    }
  }
}
