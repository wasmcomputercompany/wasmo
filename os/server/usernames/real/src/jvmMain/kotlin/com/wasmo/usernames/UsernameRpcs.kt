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
import com.wasmo.identifiers.AccountPassword
import com.wasmo.identifiers.PasswordCheckPermitType
import com.wasmo.identifiers.UsernameSlug
import com.wasmo.passwords.PasswordStore
import com.wasmo.permits.PermitService
import com.wasmo.permits.RateLimit
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import org.jetbrains.annotations.VisibleForTesting
import wasmo.sql.SqlDatabase
import wasmox.sql.SqlTransaction
import wasmox.sql.transaction

val PasswordAuthenticationRateLimit = RateLimit(
  count = 5,
  duration = 5.minutes,
)

@Inject
@ClassKey
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class LinkUsernameRpc(
  private val client: Client,
  private val wasmoDb: SqlDatabase,
  private val callDataService: CallDataService,
  private val clock: Clock,
  private val passwordStore: PasswordStore,
  private val permitService: PermitService,
) : RpcAction<LinkUsernameRequest, LinkUsernameResponse> {

  override suspend operator fun invoke(request: LinkUsernameRequest, url: Url) =
    handle(username = request.username, accountPassword = AccountPassword(secretValue = request.password) )

  @VisibleForTesting
  suspend fun handle(username: UsernameSlug, accountPassword: AccountPassword): Response<LinkUsernameResponse> =
    wasmoDb.transaction {
      val decision = decide(username, accountPassword)
      Response(
        body = LinkUsernameResponse(
          decision = decision,
          account = if (decision == LinkUsernameDecision.Success) callDataService.accountSnapshot() else null
        )
      )
    }

  context(sqlTransaction: SqlTransaction)
  private suspend fun decide(username: UsernameSlug, accountPassword: AccountPassword): LinkUsernameDecision {
    val now = clock.now()
    val permitAcquired = permitService.tryAcquire(
      now = now,
      type = PasswordCheckPermitType,
      value = username.value,
      rateLimit = PasswordAuthenticationRateLimit,
    )
    if (!permitAcquired) return LinkUsernameDecision.TooManyAttempts

    val existingUsername = selectLinkedUsernameOrNullAllowDeleted(username = username)
    return if (existingUsername?.deletedAt != null) {
      LinkUsernameDecision.UsernameDeleted
    } else if (existingUsername == null) {
      LinkUsernameDecision.UsernameNotFound
    } else if (
      // TODO: Move the isPasswordCorrect() check to a place that's not specific to usernames.
      // Perhaps client.signIn(), but the operation must be able to fail.
      !passwordStore.checkPassword(existingUsername.accountId, accountPassword)
    ) {
      // For username based sign-in specifically, the existence of the account is not secret,
      // so we can leak the fact that the account exists but password authentication failed.
      LinkUsernameDecision.PasswordAuthenticationFailed
    } else {
      client.signIn(
        sourceAccountId = client.getOrCreateAccountId(),
        targetAccountId = existingUsername.accountId,
      )
      // On success, release the permit back into the pool rather than consuming it.
      permitService.tryAcquire(
        now = now,
        type = PasswordCheckPermitType,
        value = username.value,
        rateLimit = PasswordAuthenticationRateLimit,
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
  private val passwordStore: PasswordStore,
  private val permitService: PermitService,
) : RpcAction<CreateUsernameRequest, CreateUsernameResponse> {

  override suspend fun invoke(request: CreateUsernameRequest, url: Url) =
    handle(
      username = request.username,
      password = AccountPassword(request.password),
    )

  @VisibleForTesting
  suspend fun handle(username: UsernameSlug, password: AccountPassword): Response<CreateUsernameResponse> =
    wasmoDb.transaction {
      val decision = decide(username, password)
      Response(
        body = CreateUsernameResponse(
          decision = decision,
          account = if (decision == CreateUsernameDecision.Success) callDataService.accountSnapshot() else null
        )
      )
    }

  context(sqlTransaction: SqlTransaction)
  private suspend fun decide(username: UsernameSlug, password: AccountPassword): CreateUsernameDecision {
    val now = clock.now()
    val permitAcquired = permitService.tryAcquire(
      now = now,
      type = PasswordCheckPermitType,
      value = username.value,
      rateLimit = PasswordAuthenticationRateLimit,
    )
    if (!permitAcquired) return CreateUsernameDecision.TooManyAttempts
    val existingUsername = selectLinkedUsernameOrNullAllowDeleted(username = username)
    return if (existingUsername?.deletedAt != null) {
      CreateUsernameDecision.UsernameDeleted
    } else if (existingUsername != null) {
      CreateUsernameDecision.UsernameTaken
    } else {
      val accountId = client.getOrCreateAccountId()
      // Note: If the client was already signed-in and the account has a username, then
      // attempting to attach a second username to the same account throws at the SQL level
      // because it violates a UNIQUE constraint.
      insertUsername(
        createdAt = now,
        accountId = accountId,
        usernameSlug = username
      )
      passwordStore.setPassword(
        accountId,
        oldPassword = AccountPassword.EMPTY,
        newPassword = password,
      )
      // On success, release the permit back into the pool rather than consuming it.
      permitService.tryAcquire(
        now = now,
        type = PasswordCheckPermitType,
        value = username.value,
        rateLimit = PasswordAuthenticationRateLimit,
      )
      CreateUsernameDecision.Success
    }
  }
}
