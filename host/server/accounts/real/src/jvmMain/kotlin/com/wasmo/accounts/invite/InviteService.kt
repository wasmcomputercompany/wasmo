package com.wasmo.accounts.invite

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.api.InviteTicket
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.tokens.newToken
import com.wasmo.framework.BadRequestException
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@Inject
@SingleIn(AppScope::class)
class InviteService(
  private val clock: Clock,
  private val wasmoDbService: WasmoDbService,
) {
  context(transactionCallbacks: TransactionCallbacks)
  fun create(createdBy: Client): InviteTicket {
    val code = newToken()
    wasmoDbService.inviteQueries.insertInvite(
      created_at = clock.now(),
      created_by = createdBy.getOrCreateAccountId(),
      version = 1,
      code = code,
    )
    return InviteTicket(
      code = code,
      claimed = false,
    )
  }

  context(transactionCallbacks: TransactionCallbacks)
  fun claim(claimedBy: Client, code: String): InviteTicket {
    val invite = wasmoDbService.inviteQueries.findInvitesByCode(code)
      .executeAsOneOrNull()
      ?: throw BadRequestException("unknown invite")

    val claimedById = claimedBy.getOrCreateAccountId()

    if (invite.claimed_by != claimedById) {
      if (invite.claimed_by != null) throw BadRequestException("already claimed")
      wasmoDbService.inviteQueries.claimInvite(
        new_version = invite.version + 1,
        claimed_at = clock.now(),
        claimed_by = claimedById,
        expected_version = invite.version,
        id = invite.id,
      )
    }

    return InviteTicket(
      code = code,
      claimed = true,
    )
  }
}
