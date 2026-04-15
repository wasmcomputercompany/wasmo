package com.wasmo.accounts.invite

import com.wasmo.accounts.Client
import com.wasmo.api.InviteTicket
import com.wasmo.db.accounts.invite.claimInvite
import com.wasmo.db.accounts.invite.findInvitesByCode
import com.wasmo.db.accounts.invite.insertInvite
import com.wasmo.framework.ArgumentUserException
import com.wasmo.framework.NotFoundUserException
import com.wasmo.identifiers.OsScope
import com.wasmo.sql.SqlTransaction
import com.wasmo.support.tokens.newToken
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@Inject
@SingleIn(OsScope::class)
class InviteService(
  private val clock: Clock,
) {
  context(sqlTransaction: SqlTransaction)
  suspend fun create(createdBy: Client): InviteTicket {
    val code = newToken()
    insertInvite(
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

  context(sqlTransaction: SqlTransaction)
  suspend fun claim(claimedBy: Client, code: String): InviteTicket {
    val invite = findInvitesByCode(code)
      ?: throw NotFoundUserException("unknown invite")

    val claimedById = claimedBy.getOrCreateAccountId()

    if (invite.claimed_by != claimedById) {
      if (invite.claimed_by != null) throw ArgumentUserException("already claimed")
      claimInvite(
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
