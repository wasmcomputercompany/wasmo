package com.wasmo.accounts

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.db.AccountQueries
import com.wasmo.db.CookieQueries
import com.wasmo.identifiers.AccountId
import kotlin.time.Clock

/**
 * This holds a single session cookie and uses it to find the corresponding account.
 *
 * Each account may have multiple cookies issued to it.
 */
class CookieClient private constructor(
  private val clock: Clock,
  private val cookieQueries: CookieQueries,
  private val accountQueries: AccountQueries,
  val sessionCookie: SessionCookie,
  override val userAgent: String?,
  override val ip: String?,
) : Client {
  private var cachedAccountId: AccountId? = null

  context(transactionCallbacks: TransactionCallbacks)
  override fun getAccountIdOrNull(): AccountId? {
    val cachedAccountId = cachedAccountId
    if (cachedAccountId != null) return cachedAccountId

    val cookie = cookieQueries.findCookieByToken(sessionCookie.token).executeAsOneOrNull()
      ?: return null
    return cookie.account_id
      .also { this.cachedAccountId = it }
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun getOrCreateAccountId(): AccountId {
    val cachedAccountId = cachedAccountId
    if (cachedAccountId != null) return cachedAccountId

    val cookie = cookieQueries.findCookieByToken(sessionCookie.token).executeAsOneOrNull()
    if (cookie != null) return cookie.account_id

    val accountId = accountQueries.insertAccount(
      version = 1,
    ).executeAsOne()

    cookieQueries.insertCookie(
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

  class Factory(
    private val clock: Clock,
    private val cookieQueries: CookieQueries,
    private val accountQueries: AccountQueries,
  ) {
    fun create(
      sessionCookie: SessionCookie,
      userAgent: String? = null,
      ip: String? = null,
    ) = CookieClient(
      clock = clock,
      cookieQueries = cookieQueries,
      accountQueries = accountQueries,
      sessionCookie = sessionCookie,
      userAgent = userAgent,
      ip = ip,
    )
  }
}
