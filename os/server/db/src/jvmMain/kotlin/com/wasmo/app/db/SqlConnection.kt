package com.wasmo.app.db

import com.wasmo.api.WasmoJson
import com.wasmo.app.db.RealSqlCursor
import com.wasmo.app.db.RealSqlCursor as SqlCursor
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
import wasmo.sql.RowIterator
import wasmo.sql.SqlBinder
import wasmo.sql.SqlConnection
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlRow

suspend fun <T> SqlDatabase.transactionWithResult(
  noEnclosing: Boolean,
  block: suspend context(SqlTransaction) () -> T,
): T {
  return transaction(noEnclosing, block)
}

suspend fun <T> SqlDatabase.transaction(
  noEnclosing: Boolean,
  block: suspend context(SqlTransaction) () -> T,
): T {
  val transaction = RealSqlTransaction(newConnection())
  transaction.use { transaction ->
    context(transaction) {
      val result = block()
      for (action in transaction.afterCommitActions) {
        action()
      }
      return result
    }
  }
}

suspend fun <T> SqlDatabase.withConnection(block: suspend context(SqlConnection) () -> T): T {
  newConnection().use { connection ->
    context(connection) {
      return block()
    }
  }
}

suspend fun <R> SqlConnection.executeQuery(
  sql: String,
  mapper: suspend (SqlCursor) -> R,
  binders: (SqlBinder.() -> Unit)?,
): R {
  val rowIterator = executeQuery(sql, binders)
  return mapper(RealSqlCursor(rowIterator))
}

interface SqlTransaction : SqlConnection {
  fun afterCommit(function: () -> Unit)
}

open class RealSqlTransaction(
  sqlConnection: SqlConnection,
) : SqlConnection by sqlConnection, SqlTransaction {
  val afterCommitActions = mutableListOf<() -> Unit>()

  override fun afterCommit(function: () -> Unit) {
    afterCommitActions += function
  }
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

inline fun <reified T> SqlBinder.bindJson(index: Int, value: T) {
  bindString(index, WasmoJson.encodeToString(value))
}

inline fun <reified T> SqlRow.getJson2(index: Int): T {
  val string = getString(index)!!
  return WasmoJson.decodeFromString<T>(string)
}

fun SqlBinder.bindAccountId(index: Int, value: AccountId?) = bindS64(index, value?.id)
fun SqlBinder.bindAppSlug(index: Int, value: AppSlug?) = bindString(index, value?.value)
fun SqlBinder.bindComputerAccessId(index: Int, value: ComputerAccessId?) = bindS64(index, value?.id)
fun SqlBinder.bindComputerAllocationId(index: Int, value: ComputerAllocationId?) = bindS64(index, value?.id)
fun SqlBinder.bindComputerId(index: Int, value: ComputerId?) = bindS64(index, value?.id)
fun SqlBinder.bindComputerSlug(index: Int, value: ComputerSlug?) = bindString(index, value?.value)
fun SqlBinder.bindComputerSpecId(index: Int, value: ComputerSpecId?) = bindS64(index, value?.id)
fun SqlBinder.bindCookieId(index: Int, value: CookieId?) = bindS64(index, value?.id)
fun SqlBinder.bindInstalledAppId(index: Int, value: InstalledAppId?) = bindS64(index, value?.id)
fun SqlBinder.bindInstalledAppReleaseId(index: Int, value: InstalledAppReleaseId?) = bindS64(index, value?.id)
fun SqlBinder.bindInviteId(index: Int, value: InviteId?) = bindS64(index, value?.id)
fun SqlBinder.bindPasskeyId(index: Int, value: PasskeyId?) = bindS64(index, value?.id)
fun SqlBinder.bindStripeCustomerId(index: Int, value: StripeCustomerId?) = bindS64(index, value?.id)
fun SqlBinder.bindWasmoFileAddress(index: Int, value: WasmoFileAddress?) = bindString(index, value?.toString())

fun SqlRow.getAccountId(index: Int) = AccountId(getS64(index)!!)
fun SqlRow.getAccountIdOrNull(index: Int) = getS64(index)?.let { AccountId(it) }
fun SqlRow.getAppSlug(index: Int) = AppSlug(getString(index)!!)
fun SqlRow.getComputerAccessId(index: Int) = ComputerAccessId(getS64(index)!!)
fun SqlRow.getComputerAccessIdOrNull(index: Int) = getS64(index)?.let { ComputerAccessId(it) }
fun SqlRow.getComputerAllocationId(index: Int) = ComputerAllocationId(getS64(index)!!)
fun SqlRow.getComputerAllocationIdOrNull(index: Int) = getS64(index)?.let { ComputerAllocationId(it) }
fun SqlRow.getComputerId(index: Int) = ComputerId(getS64(index)!!)
fun SqlRow.getComputerIdOrNull(index: Int) = getS64(index)?.let { ComputerId(it) }
fun SqlRow.getComputerSlug(index: Int) = ComputerSlug(getString(index)!!)
fun SqlRow.getComputerSpecId(index: Int) = ComputerSpecId(getS64(index)!!)
fun SqlRow.getComputerSpecIdOrNull(index: Int) = getS64(index)?.let { ComputerSpecId(it) }
fun SqlRow.getCookieId(index: Int) = CookieId(getS64(index)!!)
fun SqlRow.getCookieIdOrNull(index: Int) = getS64(index)?.let { CookieId(it) }
fun SqlRow.getInstalledAppId(index: Int) = InstalledAppId(getS64(index)!!)
fun SqlRow.getInstalledAppIdOrNull(index: Int) = getS64(index)?.let { InstalledAppId(it) }
fun SqlRow.getInstalledAppReleaseId(index: Int) = InstalledAppReleaseId(getS64(index)!!)
fun SqlRow.getInstalledAppReleaseIdOrNull(index: Int) = getS64(index)?.let { InstalledAppReleaseId(it) }
fun SqlRow.getInviteId(index: Int) = InviteId(getS64(index)!!)
fun SqlRow.getPasskeyId(index: Int) = PasskeyId(getS64(index)!!)
fun SqlRow.getStripeCustomerId(index: Int) = StripeCustomerId(getS64(index)!!)
fun SqlRow.getWasmoFileAddress(index: Int) = getString(index)!!.toWasmoFileAddress()
