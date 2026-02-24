package com.wasmo.accounts.invite

import com.wasmo.accounts.AccountStore
import com.wasmo.accounts.Client
import com.wasmo.api.InviteTicket
import com.wasmo.app.db.WasmoDbService
import com.wasmo.website.ServerAppPage

class InvitePageAction(
  private val client: Client,
  private val accountStoreFactory: AccountStore.Factory,
  private val appPageFactory: ServerAppPage.Factory,
  private val wasmoDbService: WasmoDbService,
) {
  fun invite(code: String): ServerAppPage {
    // TODO: associate code with the current user's session?
    val accountStore = accountStoreFactory.create(client)
    val inviteTicket = InviteTicket(
      claimed = false,
      code = code,
    )
    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      appPageFactory.create(
        accountSnapshot = accountStore.snapshot(),
        inviteTicket = inviteTicket,
      )
    }
  }
}
