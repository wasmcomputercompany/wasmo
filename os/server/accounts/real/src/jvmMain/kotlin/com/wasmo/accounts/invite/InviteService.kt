package com.wasmo.accounts.invite

import com.wasmo.accounts.Client
import com.wasmo.api.InviteTicket
import com.wasmo.db.accounts.invite.claimInvite
import com.wasmo.db.accounts.invite.findInvitesByCode
import com.wasmo.db.accounts.invite.insertInvite
import com.wasmo.framework.ArgumentUserException
import com.wasmo.framework.NotFoundUserException
import com.wasmo.identifiers.OsScope
import com.wasmo.support.tokens.newToken
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import wasmox.sql.SqlTransaction

@Inject
@SingleIn(OsScope::class)
class InviteService(
  private val clock: Clock,
) {
  context(sqlTransaction: SqlTransaction)
  suspend fun create(createdBy: Client): InviteTicket {
    val code = newToken()
    insertInvite(
      createdAt = clock.now(),
      createdBy = createdBy.getOrCreateAccountId(),
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

    if (invite.claimedBy != claimedById) {
      if (invite.claimedBy != null) throw ArgumentUserException("already claimed")
      claimInvite(
        newVersion = invite.version + 1,
        claimedAt = clock.now(),
        claimedBy = claimedById,
        expectedVersion = invite.version,
        id = invite.id,
      )
    }

    return InviteTicket(
      code = code,
      claimed = true,
    )
  }
}
