package com.wasmo.accounts

import com.wasmo.app.db2.WasmoDbTransaction as TransactionCallbacks
import com.wasmo.identifiers.AccountId
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
  override val challenger: Challenger = hmacChallengerFactory.create(sessionCookie.token)

  private var cachedAccountId: AccountId? = null

  context(transactionCallbacks: TransactionCallbacks)
  override suspend fun getAccountIdOrNull(): AccountId? {
    val cachedAccountId = cachedAccountId
    if (cachedAccountId != null) return cachedAccountId

    val cookie = transactionCallbacks.cookieQueries.findCookieByToken(sessionCookie.token).executeAsOneOrNull()
      ?: return null
    return cookie.account_id
      .also { this.cachedAccountId = it }
  }

  context(transactionCallbacks: TransactionCallbacks)
  override suspend fun getOrCreateAccountId(): AccountId {
    val cachedAccountId = cachedAccountId
    if (cachedAccountId != null) return cachedAccountId

    val cookie = transactionCallbacks.cookieQueries.findCookieByToken(sessionCookie.token).executeAsOneOrNull()
    if (cookie != null) return cookie.account_id

    val accountId = transactionCallbacks.accountQueries.insertAccount(
      version = 1,
    )

    transactionCallbacks.cookieQueries.insertCookie(
      created_at = clock.now(),
      account_id = accountId,
      token = sessionCookie.token,
      created_by_user_agent = userAgent,
      created_by_ip = ip,
    )

    return accountId
      .also { this.cachedAccountId = it }
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun invalidate() {
    this.cachedAccountId = null
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
