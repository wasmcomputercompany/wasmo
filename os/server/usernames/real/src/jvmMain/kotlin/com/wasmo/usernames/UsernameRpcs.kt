package com.wasmo.usernames

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.CreateUsernameDecision
import com.wasmo.api.CreateUsernameRequest
import com.wasmo.api.CreateUsernameResponse
import com.wasmo.api.LinkUsernameDecision
import com.wasmo.api.LinkUsernameRequest
import com.wasmo.api.LinkUsernameResponse
import com.wasmo.calls.CallDataService
import com.wasmo.db.usernames.insertUsername
import com.wasmo.db.usernames.selectLinkedUsernameOrNullAllowDeleted
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import com.wasmo.identifiers.UsernameSlug
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.time.Clock
import org.jetbrains.annotations.VisibleForTesting
import wasmo.sql.SqlDatabase
import wasmox.sql.SqlTransaction
import wasmox.sql.transaction

@Inject
@ClassKey
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class LinkUsernameRpc(
  private val client: Client,
  private val wasmoDb: SqlDatabase,
  private val callDataService: CallDataService,
) : RpcAction<LinkUsernameRequest, LinkUsernameResponse> {

  override suspend operator fun invoke(request: LinkUsernameRequest, url: Url) =
    handle(request.username)

  @VisibleForTesting
  suspend fun handle(username: UsernameSlug): Response<LinkUsernameResponse> =
    wasmoDb.transaction {
      val decision = decide(username)
      Response(
        body = LinkUsernameResponse(
          decision = decision,
          account = if (decision == LinkUsernameDecision.Success) callDataService.accountSnapshot() else null
        )
      )
    }

  context(sqlTransaction: SqlTransaction)
  private suspend fun decide(username: UsernameSlug): LinkUsernameDecision {
    val existingUsername = selectLinkedUsernameOrNullAllowDeleted(username = username)
    return if (existingUsername?.deletedAt != null) {
      LinkUsernameDecision.UsernameDeleted
    } else if (existingUsername == null) {
      LinkUsernameDecision.UsernameNotFound
    } else {
      client.signIn(
        sourceAccountId = client.getOrCreateAccountId(),
        targetAccountId = existingUsername.accountId,
      )
      LinkUsernameDecision.Success
    }
  }
}

@Inject
@ClassKey
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class CreateUsernameRpc(
  private val client: Client,
  private val wasmoDb: SqlDatabase,
  private val callDataService: CallDataService,
  private val clock: Clock,
) : RpcAction<CreateUsernameRequest, CreateUsernameResponse> {

  override suspend fun invoke(request: CreateUsernameRequest, url: Url) =
    handle(request.username)

  @VisibleForTesting
  suspend fun handle(username: UsernameSlug): Response<CreateUsernameResponse> =
    wasmoDb.transaction {
      val decision = decide(username)
      Response(
        body = CreateUsernameResponse(
          decision = decision,
          account = if (decision == CreateUsernameDecision.Success) callDataService.accountSnapshot() else null
        )
      )
    }

  context(sqlTransaction: SqlTransaction)
  private suspend fun decide(username: UsernameSlug): CreateUsernameDecision {
    val existingUsername = selectLinkedUsernameOrNullAllowDeleted(username = username)
    return if (existingUsername?.deletedAt != null) {
      CreateUsernameDecision.UsernameDeleted
    } else if (existingUsername != null) {
      CreateUsernameDecision.UsernameTaken
    } else {
      insertUsername(
        createdAt = clock.now(),
        accountId = client.getOrCreateAccountId(),
        usernameSlug = username
      )
      CreateUsernameDecision.Success
    }
  }
}
