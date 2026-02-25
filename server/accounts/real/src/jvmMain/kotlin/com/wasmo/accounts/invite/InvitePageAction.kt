package com.wasmo.accounts.invite

import com.wasmo.accounts.AccountStore
import com.wasmo.accounts.Client
import com.wasmo.api.InviteTicket
import com.wasmo.app.db.WasmoDbService
import com.wasmo.framework.NotFoundException
import com.wasmo.website.ServerAppPage

class InvitePageAction(
  private val client: Client,
  private val accountStoreFactory: AccountStore.Factory,
  private val appPageFactory: ServerAppPage.Factory,
  private val wasmoDbService: WasmoDbService,
) {
  fun invite(code: String): ServerAppPage {
    val accountStore = accountStoreFactory.create(client)
    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val invite = wasmoDbService.inviteQueries.findInvitesByCode(code)
        .executeAsOneOrNull()
        ?: throw NotFoundException()

      appPageFactory.create(
        accountSnapshot = accountStore.snapshot(),
        inviteTicket = InviteTicket(
          claimed = invite.claimed_by != null,
          code = invite.code,
        ),
      )
    }
  }
}
