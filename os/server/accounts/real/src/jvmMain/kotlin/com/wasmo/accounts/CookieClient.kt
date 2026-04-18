package com.wasmo.accounts

import com.wasmo.db.accounts.findCookieByToken
import com.wasmo.db.accounts.insertAccount
import com.wasmo.db.accounts.insertCookie
import com.wasmo.db.accounts.updateAccountIdByAccountId
import com.wasmo.identifiers.AccountId
import com.wasmo.sql.SqlTransaction
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.time.Clock

/**
 * This holds a single session cookie and uses it to find the corresponding account.
 *
 * Each account may have multiple cookies issued to it.
 */
@AssistedInject
class CookieClient(
  private val clock: Clock,
  @Assisted private val sessionCookie: SessionCookie,
  @Assisted override val userAgent: String?,
  @Assisted override val ip: String?,
  hmacChallengerFactory: HmacChallenger.Factory,
) : Client {
  private var listeners = listOf<Client.Listener>()

  override val challenger: Challenger = hmacChallengerFactory.create(sessionCookie.token)

  private var cachedAccountId: AccountId? = null

  override fun addListener(listener: Client.Listener) {
    listeners += listener
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun signIn(
    source: AccountId,
    target: AccountId,
  ) {
    if (source == target) return // Nothing to do.

    updateAccountIdByAccountId(
      target_account_id = target,
      source_account_id = source,
    )
    invalidate()
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun getAccountIdOrNull(): AccountId? {
    val cachedAccountId = cachedAccountId
    if (cachedAccountId != null) return cachedAccountId

    val cookie = findCookieByToken(sessionCookie.token)
      ?: return null
    return cookie.account_id
      .also { this.cachedAccountId = it }
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun getOrCreateAccountId(): AccountId {
    val cachedAccountId = cachedAccountId
    if (cachedAccountId != null) return cachedAccountId

    val cookie = findCookieByToken(sessionCookie.token)
    if (cookie != null) return cookie.account_id

    val accountId = insertAccount(
      version = 1,
    )

    insertCookie(
      created_at = clock.now(),
      account_id = accountId,
      token = sessionCookie.token,
      created_by_user_agent = userAgent,
      created_by_ip = ip,
    )

    return accountId
      .also { this.cachedAccountId = it }
  }

  context(sqlTransaction: SqlTransaction)
  override fun invalidate() {
    this.cachedAccountId = null

    for (listener in listeners) {
      listener.onInvalidate()
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(
      sessionCookie: SessionCookie,
      userAgent: String? = null,
      ip: String? = null,
    ): CookieClient
  }
}
