package com.wasmo.usernames
import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.CreateUsernameRequest
import com.wasmo.api.CreateUsernameResponse
import com.wasmo.api.LinkUsernameRequest
import com.wasmo.api.LinkUsernameResponse
import com.wasmo.api.UsernameDecision
import com.wasmo.calls.CallDataService
import com.wasmo.db.usernames.insertUsername
import com.wasmo.db.usernames.selectLinkedUsernameOrNull
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import com.wasmo.identifiers.UsernameSlug
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.time.Clock
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlException
import wasmox.sql.SqlTransaction
import wasmox.sql.transaction

@Inject
@ClassKey(LinkUsernameRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class LinkUsernameRpc(
  private val client: Client,
  private val clock: Clock,
  private val wasmoDb: SqlDatabase,
  private val callDataService: CallDataService,
) : RpcAction<LinkUsernameRequest, LinkUsernameResponse> {
  override suspend operator fun invoke(
    request: LinkUsernameRequest,
    url: Url,
  ) = handle(request)

  suspend fun handle(request: LinkUsernameRequest) =
    usernameRpc(client, clock, wasmoDb, callDataService, request.username, Mode.LinkExisting).let {
     Response(body = LinkUsernameResponse(it.decision, it.account))
    }
}

@Inject
@ClassKey(CreateUsernameRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class CreateUsernameRpc(
  private val client: Client,
  private val clock: Clock,
  private val wasmoDb: SqlDatabase,
  private val callDataService: CallDataService,
) : RpcAction<CreateUsernameRequest, CreateUsernameResponse> {
  override suspend fun invoke(
    request: CreateUsernameRequest,
    url: Url,
  ): Response<CreateUsernameResponse> = handle(request)

  suspend fun handle(request: CreateUsernameRequest) =
    usernameRpc(client, clock, wasmoDb, callDataService, request.username, Mode.LinkNew).let {
      Response(body = CreateUsernameResponse(it.decision, it.account))
    }
}

private enum class Mode(val allowLinkExisting: Boolean, val allowLinkNew: Boolean) {
  LinkExisting(allowLinkExisting = true, allowLinkNew = false),
  LinkNew(allowLinkExisting = false, allowLinkNew = true),
}

private data class UsernameRpcResponse(val decision: UsernameDecision, val account: AccountSnapshot?)

private suspend fun usernameRpc(
  client: Client,
  clock: Clock,
  wasmoDb: SqlDatabase,
  callDataService: CallDataService,
  username: UsernameSlug,
  mode: Mode,
): UsernameRpcResponse {
  return wasmoDb.transaction {
    val decision = decideAndAct(client, clock, username, mode)
    val account = when (decision) {
      UsernameDecision.Success -> callDataService.accountSnapshot()
      else -> null
    }
    UsernameRpcResponse(decision, account)
  }
}

context(sqlTransaction: SqlTransaction)
private suspend fun decideAndAct(
  client: Client,
  clock: Clock,
  username: UsernameSlug,
  mode: Mode,
): UsernameDecision {
  try {
    val now = clock.now()
    val accountId = client.getOrCreateAccountId()
    if (mode.allowLinkExisting) {
      selectLinkedUsernameOrNull(username)?.let { dbUsername ->
        client.signIn(
          sourceAccountId = accountId,
          targetAccountId = dbUsername.accountId,
        )
        return UsernameDecision.Success // linked existing username
      }
    }
    if (mode.allowLinkNew) {
      insertUsername(
        createdAt = now,
        accountId = accountId,
        usernameSlug = username
      )
      return UsernameDecision.Success // linked new username
    }
  } catch (_: SqlException) {
    return UsernameDecision.BadRequest
  }
  return UsernameDecision.BadRequest
}
