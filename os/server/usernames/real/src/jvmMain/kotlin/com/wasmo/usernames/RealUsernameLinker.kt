package com.wasmo.usernames

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.UsernameDecision
import com.wasmo.calls.CallDataService
import com.wasmo.db.usernames.insertUsername
import com.wasmo.db.usernames.selectLinkedUsernameOrNull
import com.wasmo.db.usernames.selectLinkedUsernameOrNullAllowDeleted
import com.wasmo.identifiers.UsernameSlug
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlException
import wasmox.sql.SqlTransaction
import wasmox.sql.isDuplicateDatabase
import wasmox.sql.isUniqueViolation
import wasmox.sql.transaction

@Inject
@SingleIn(CallScope::class)
class RealUsernameLinker(
  private val client: Client,
  private val clock: Clock,
  private val wasmoDb: SqlDatabase,
  private val callDataService: CallDataService,
) : UsernameLinker() {

  override suspend fun link(
    username: UsernameSlug,
    mode: Mode,
  ): UsernameLinkResult {
    return wasmoDb.transaction {
      val decision = decideAndAct(username, mode)
      val account = when (decision) {
        UsernameDecision.Success -> callDataService.accountSnapshot()
        else -> null
      }
      UsernameLinkResult(decision, account)
    }
  }

  context(sqlTransaction: SqlTransaction)
  private suspend fun decideAndAct(
    username: UsernameSlug,
    mode: Mode,
  ): UsernameDecision {
    try {
      val now = clock.now()
      val accountId = client.getOrCreateAccountId()
      val existingUsername = selectLinkedUsernameOrNullAllowDeleted(username = username)
      if (existingUsername?.deletedAt != null) {
        return UsernameDecision.UsernameDeleted // username exists but was deleted
      }
      return when (mode) {
        Mode.LinkExisting -> {
          if (existingUsername == null) {
            // client asked to link an existing username, but it does not exist
            UsernameDecision.UsernameExistenceIncompatibleWithRequest
          } else {
            client.signIn(
              sourceAccountId = accountId,
              targetAccountId = existingUsername.accountId,
            )
            UsernameDecision.Success // linked existing username
          }
        }
        Mode.LinkNew -> {
          if (existingUsername != null) {
            // client asked to link a new username, but it doesn't exist
            UsernameDecision.UsernameExistenceIncompatibleWithRequest
          } else {
            insertUsername(
              createdAt = now,
              accountId = accountId,
              usernameSlug = username
            )
            UsernameDecision.Success // linked new username
          }
        }
      }
    } catch (e: SqlException) {
      return if (e.isUniqueViolation || e.isDuplicateDatabase) {
        // Internal server error (non-unique username should have been handled above).
        // Let higher-level code deal with the uncaught exception.
        throw e
      } else {
        UsernameDecision.BadRequest
      }
    }
  }
}
