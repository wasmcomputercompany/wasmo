package com.wasmo.app.db2

import app.cash.sqldelight.db.SqlPreparedStatement
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
import kotlin.time.Instant
import okio.ByteString.Companion.toByteString
import okio.Closeable
import wasmo.sql.RowIterator
import wasmo.sql.SqlBinder
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow

interface WasmoDbConnection : Closeable {
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

  suspend fun execute(
    identifier: Int?,
    sql: String,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): Long

  suspend fun <R> executeQuery(
    identifier: Int?,
    sql: String,
    mapper: suspend (SqlCursor) -> R,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): R
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
) : WasmoDbConnection {
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
    get() = PasskeyQueries(this, PasskeyAdapter)
  override val stripeCustomerQueries: StripeCustomerQueries
    get() = StripeCustomerQueries(this, StripeCustomerAdapter)

  override fun close() {
    sqlConnection.close()
  }

  override suspend fun execute(
    identifier: Int?,
    sql: String,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): Long {
    try {
      return sqlConnection.execute(
        sql,
        bindParameters = {
          binders?.invoke(RealSqlPreparedStatement(this))
        },
      )
    } catch (e: Exception) {
      throw e
    }
  }

  override suspend fun <R> executeQuery(
    identifier: Int?,
    sql: String,
    mapper: suspend (SqlCursor) -> R,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): R {
    try {
      val rowIterator = sqlConnection.executeQuery(
        sql,
        bindParameters = {
          binders?.invoke(RealSqlPreparedStatement(this))
        },
      )
      return mapper(RealSqlCursor(rowIterator))
    } catch (e: Exception) {
      throw e
    }
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

class RealSqlPreparedStatement(
  val sqlBinder: SqlBinder,
) : SqlPreparedStatement {
  override fun bindBoolean(index: Int, boolean: Boolean?) {
    sqlBinder.bindBool(index, boolean)
  }

  override fun bindBytes(index: Int, bytes: ByteArray?) {
    sqlBinder.bindBytes(index, bytes?.toByteString())
  }

  override fun bindDouble(index: Int, double: Double?) {
    sqlBinder.bindF64(index, double)
  }

  fun bindInt(index: Int, value: Int?) {
    sqlBinder.bindS32(index, value)
  }

  override fun bindLong(index: Int, long: Long?) {
    sqlBinder.bindS64(index, long)
  }

  override fun bindString(index: Int, string: String?) {
    sqlBinder.bindString(index, string)
  }

  fun bindInstant(index: Int, value: Instant?) {
    sqlBinder.bindInstant(index, value)
  }
}
