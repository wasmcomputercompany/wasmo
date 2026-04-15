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
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerAccessId
import com.wasmo.identifiers.ComputerAllocationId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ComputerSpecId
import com.wasmo.identifiers.CookieId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.identifiers.InviteId
import com.wasmo.identifiers.PasskeyId
import com.wasmo.identifiers.StripeCustomerId
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.identifiers.WasmoFileAddress.Companion.toWasmoFileAddress
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
    get() = AccountQueries(this)
  override val computerQueries: ComputerQueries
    get() = ComputerQueries(this)
  override val computerAccessQueries: ComputerAccessQueries
    get() = ComputerAccessQueries(this)
  override val computerAllocationQueries: ComputerAllocationQueries
    get() = ComputerAllocationQueries(this)
  override val computerSpecQueries: ComputerSpecQueries
    get() = ComputerSpecQueries(this)
  override val cookieQueries: CookieQueries
    get() = CookieQueries(this)
  override val installedAppQueries: InstalledAppQueries
    get() = InstalledAppQueries(this)
  override val installedAppReleaseQueries: InstalledAppReleaseQueries
    get() = InstalledAppReleaseQueries(this)
  override val inviteQueries: InviteQueries
    get() = InviteQueries(this)
  override val passkeyQueries: PasskeyQueries
    get() = PasskeyQueries(this)
  override val stripeCustomerQueries: StripeCustomerQueries
    get() = StripeCustomerQueries(this)
}

suspend fun <T> RowIterator.list(mapper: (SqlRow) -> T) : List<T> {
  use {
    return buildList {
      while (true) {
        val row = next() ?: break
        add(mapper(row))
      }
    }
  }
}

suspend fun <T> RowIterator.single(mapper: (SqlRow) -> T) : T {
  use {
    val row = next() ?: error("expected one element but was none")
    val result = mapper(row)
    check(next() == null) { "expected one element but was multiple "}
    return result
  }
}

suspend fun <T> RowIterator.singleOrNull(mapper: (SqlRow) -> T) : T? {
  use {
    val row = next() ?: return null
    val result = mapper(row)
    check(next() == null) { "expected at most one element but was multiple "}
    return result
  }
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

inline fun <reified T> SqlRow.getJson2(index: Int): T {
  val string = getString(index)!!
  return WasmoJson.decodeFromString<T>(string)
}

fun RealSqlCursor.getAccountId(index: Int) = AccountId(getS64(index)!!)
fun RealSqlCursor.getComputerAccessId(index: Int) = ComputerAccessId(getS64(index)!!)
fun RealSqlCursor.getComputerAllocationId(index: Int) = ComputerAllocationId(getS64(index)!!)
fun RealSqlCursor.getComputerId(index: Int) = ComputerId(getS64(index)!!)
fun RealSqlCursor.getComputerSpecId(index: Int) = ComputerSpecId(getS64(index)!!)
fun RealSqlCursor.getCookieId(index: Int) = CookieId(getS64(index)!!)
fun RealSqlCursor.getInstalledAppId(index: Int) = InstalledAppId(getS64(index)!!)
fun RealSqlCursor.getInstalledAppReleaseId(index: Int) = InstalledAppReleaseId(getS64(index)!!)
fun RealSqlCursor.getInviteId(index: Int) = InviteId(getS64(index)!!)
fun RealSqlCursor.getPasskeyId(index: Int) = PasskeyId(getS64(index)!!)
fun RealSqlCursor.getStripeCustomerId(index: Int) = StripeCustomerId(getS64(index)!!)
fun RealSqlCursor.getAccountIdOrNull(index: Int) = getS64(index)?.let { AccountId(it) }
fun RealSqlCursor.getComputerAccessIdOrNull(index: Int) = getS64(index)?.let { ComputerAccessId(it) }
fun RealSqlCursor.getComputerAllocationIdOrNull(index: Int) = getS64(index)?.let { ComputerAllocationId(it) }
fun RealSqlCursor.getComputerIdOrNull(index: Int) = getS64(index)?.let { ComputerId(it) }
fun RealSqlCursor.getComputerSpecIdOrNull(index: Int) = getS64(index)?.let { ComputerSpecId(it) }
fun RealSqlCursor.getCookieIdOrNull(index: Int) = getS64(index)?.let { CookieId(it) }
fun RealSqlCursor.getInstalledAppIdOrNull(index: Int) = getS64(index)?.let { InstalledAppId(it) }
fun RealSqlCursor.getInstalledAppReleaseIdOrNull(index: Int) = getS64(index)?.let { InstalledAppReleaseId(it) }

fun SqlRow.getAccountId(index: Int) = AccountId(getS64(index)!!)
fun SqlRow.getComputerAccessId(index: Int) = ComputerAccessId(getS64(index)!!)
fun SqlRow.getComputerAllocationId(index: Int) = ComputerAllocationId(getS64(index)!!)
fun SqlRow.getComputerId(index: Int) = ComputerId(getS64(index)!!)
fun SqlRow.getComputerSpecId(index: Int) = ComputerSpecId(getS64(index)!!)
fun SqlRow.getCookieId(index: Int) = CookieId(getS64(index)!!)
fun SqlRow.getInstalledAppId(index: Int) = InstalledAppId(getS64(index)!!)
fun SqlRow.getInstalledAppReleaseId(index: Int) = InstalledAppReleaseId(getS64(index)!!)
fun SqlRow.getInviteId(index: Int) = InviteId(getS64(index)!!)
fun SqlRow.getPasskeyId(index: Int) = PasskeyId(getS64(index)!!)
fun SqlRow.getStripeCustomerId(index: Int) = StripeCustomerId(getS64(index)!!)
fun SqlRow.getAccountIdOrNull(index: Int) = getS64(index)?.let { AccountId(it) }
fun SqlRow.getComputerAccessIdOrNull(index: Int) = getS64(index)?.let { ComputerAccessId(it) }
fun SqlRow.getComputerAllocationIdOrNull(index: Int) = getS64(index)?.let { ComputerAllocationId(it) }
fun SqlRow.getComputerIdOrNull(index: Int) = getS64(index)?.let { ComputerId(it) }
fun SqlRow.getComputerSpecIdOrNull(index: Int) = getS64(index)?.let { ComputerSpecId(it) }
fun SqlRow.getCookieIdOrNull(index: Int) = getS64(index)?.let { CookieId(it) }
fun SqlRow.getInstalledAppIdOrNull(index: Int) = getS64(index)?.let { InstalledAppId(it) }
fun SqlRow.getInstalledAppReleaseIdOrNull(index: Int) = getS64(index)?.let { InstalledAppReleaseId(it) }

fun SqlBinder.bindAccountId(index: Int, value: AccountId?) = bindS64(index, value?.id)
fun SqlBinder.bindComputerAccessId(index: Int, value: ComputerAccessId?) = bindS64(index, value?.id)
fun SqlBinder.bindComputerAllocationId(index: Int, value: ComputerAllocationId?) = bindS64(index, value?.id)
fun SqlBinder.bindComputerId(index: Int, value: ComputerId?) = bindS64(index, value?.id)
fun SqlBinder.bindComputerSpecId(index: Int, value: ComputerSpecId?) = bindS64(index, value?.id)
fun SqlBinder.bindCookieId(index: Int, value: CookieId?) = bindS64(index, value?.id)
fun SqlBinder.bindInstalledAppId(index: Int, value: InstalledAppId?) = bindS64(index, value?.id)
fun SqlBinder.bindInstalledAppReleaseId(index: Int, value: InstalledAppReleaseId?) = bindS64(index, value?.id)
fun SqlBinder.bindInviteId(index: Int, value: InviteId?) = bindS64(index, value?.id)
fun SqlBinder.bindPasskeyId(index: Int, value: PasskeyId?) = bindS64(index, value?.id)
fun SqlBinder.bindStripeCustomerId(index: Int, value: StripeCustomerId?) = bindS64(index, value?.id)

fun RealSqlCursor.getAppSlug(index: Int) = AppSlug(getString(index)!!)
fun SqlBinder.bindAppSlug(index: Int, value: AppSlug?) = bindString(index, value?.value)
fun RealSqlCursor.getComputerSlug(index: Int) = ComputerSlug(getString(index)!!)
fun SqlBinder.bindComputerSlug(index: Int, value: ComputerSlug?) = bindString(index, value?.value)

fun SqlRow.getAppSlug(index: Int) = AppSlug(getString(index)!!)
fun SqlRow.getComputerSlug(index: Int) = ComputerSlug(getString(index)!!)

fun RealSqlCursor.getWasmoFileAddress(index: Int) = getString(index)!!.toWasmoFileAddress()
fun SqlBinder.bindWasmoFileAddress(index: Int, value: WasmoFileAddress?) = bindString(index, value?.toString())

