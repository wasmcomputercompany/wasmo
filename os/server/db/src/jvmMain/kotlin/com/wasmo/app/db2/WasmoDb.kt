package com.wasmo.app.db2

import com.wasmo.api.WasmoJson
import com.wasmo.app.db.AccountQueries
import com.wasmo.app.db.ComputerAccessQueries
import com.wasmo.app.db.ComputerAllocationQueries
import com.wasmo.app.db.ComputerQueries
import com.wasmo.app.db.ComputerSpecQueries
import com.wasmo.app.db.CookieQueries
import com.wasmo.app.db.InstalledAppQueries
import com.wasmo.app.db.InstalledAppReleaseQueries
import com.wasmo.app.db.InviteQueries
import com.wasmo.app.db.PasskeyQueries
import com.wasmo.app.db.StripeCustomerQueries
import com.wasmo.app.db2.RealSqlCursor
import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.PasskeyId
import kotlin.time.Instant
import okio.Closeable
import wasmo.sql.RowIterator
import wasmo.sql.SqlBinder
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow

interface WasmoDbConnection : Closeable, SqlConnection {
  val sqlConnection: SqlConnection
  val accountQueries: AccountQueries
  val computerQueries: ComputerQueries
  val computerAccessQueries: ComputerAccessQueries
  val computerAllocationQueries: ComputerAllocationQueries
  val computerSpecQueries: ComputerSpecQueries
  val cookieQueries: CookieQueries
  val installedAppQueries: InstalledAppQueries
  val installedAppReleaseQueries: InstalledAppReleaseQueries
  val inviteQueries: InviteQueries
  val passkeyQueries: PasskeyQueries
  val stripeCustomerQueries: StripeCustomerQueries
}

suspend fun <R> SqlConnection.executeQuery(
  sql: String,
  mapper: suspend (SqlCursor) -> R,
  binders: (SqlBinder.() -> Unit)?,
): R {
  val rowIterator = executeQuery(sql, binders)
  return mapper(RealSqlCursor(rowIterator))
}

interface WasmoDbTransaction : WasmoDbConnection {
  fun afterCommit(function: () -> Unit)
}

open class RealWasmoDbTransaction(
  sqlConnection: SqlConnection,
) : RealWasmoDbConnection(sqlConnection), WasmoDbTransaction {
  val afterCommitActions = mutableListOf<() -> Unit>()

  override fun afterCommit(function: () -> Unit) {
    afterCommitActions += function
  }
}

open class RealWasmoDbConnection(
  override val sqlConnection: SqlConnection,
) : WasmoDbConnection, SqlConnection by sqlConnection {
  override val accountQueries: AccountQueries
    get() = AccountQueries(this, AccountAdapter)
  override val computerQueries: ComputerQueries
    get() = ComputerQueries(this, ComputerAdapter, ComputerAccessAdapter)
  override val computerAccessQueries: ComputerAccessQueries
    get() = ComputerAccessQueries(this, ComputerAccessAdapter)
  override val computerAllocationQueries: ComputerAllocationQueries
    get() = ComputerAllocationQueries(this, ComputerAllocationAdapter)
  override val computerSpecQueries: ComputerSpecQueries
    get() = ComputerSpecQueries(this, ComputerSpecAdapter)
  override val cookieQueries: CookieQueries
    get() = CookieQueries(this, CookieAdapter)
  override val installedAppQueries: InstalledAppQueries
    get() = InstalledAppQueries(this, InstalledAppAdapter, InstalledAppReleaseAdapter)
  override val installedAppReleaseQueries: InstalledAppReleaseQueries
    get() = InstalledAppReleaseQueries(this, InstalledAppReleaseAdapter)
  override val inviteQueries: InviteQueries
    get() = InviteQueries(this, InviteAdapter)
  override val passkeyQueries: PasskeyQueries
    get() = PasskeyQueries(this)
  override val stripeCustomerQueries: StripeCustomerQueries
    get() = StripeCustomerQueries(this, StripeCustomerAdapter)
}

class RealSqlCursor(
  private val rowIterator: RowIterator,
) {
  private var currentRow: SqlRow? = null

  suspend fun next(): Boolean {
    val next = rowIterator.next()
    currentRow = next
    return next != null
  }

  fun getBool(index: Int): Boolean? {
    return currentRow!!.getBool(index)
  }

  fun getBytes(index: Int): ByteArray? {
    return currentRow!!.getBytes(index)?.toByteArray()
  }

  fun getF64(index: Int): Double? {
    return currentRow!!.getF64(index)
  }

  fun getS64(index: Int): Long? {
    return currentRow!!.getS64(index)
  }

  fun getString(index: Int): String? {
    return currentRow!!.getString(index)
  }

  fun getInstant(index: Int): Instant? {
    return currentRow!!.getInstant(index)
  }

  fun getS32(index: Int): Int? {
    return currentRow!!.getS32(index)
  }
}

inline fun <reified T> RealSqlCursor.getJson(index: Int): T {
  val string = getString(index)!!
  return WasmoJson.decodeFromString<T>(string)
}

inline fun <reified T> SqlBinder.bindJson(index: Int, value: T) {
  bindString(index, WasmoJson.encodeToString(value))
}

fun RealSqlCursor.getAccountId(index: Int) = AccountId(getS64(index)!!)

fun SqlBinder.bindAccountId(index: Int, value: AccountId) {
  bindS64(index, value.id)
}

fun RealSqlCursor.getPasskeyId(index: Int) = PasskeyId(getS64(index)!!)

fun SqlBinder.bindPasskeyId(index: Int, value: PasskeyId) {
  bindS64(index, value.id)
}
