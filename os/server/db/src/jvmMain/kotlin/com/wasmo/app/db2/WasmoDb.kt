package com.wasmo.app.db2

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
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
import okio.ByteString.Companion.toByteString
import okio.Closeable
import wasmo.sql.RowIterator
import wasmo.sql.SqlBinder
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow

interface WasmoDbTransaction : WasmoDbConnection {
  fun afterCommit(function: () -> Unit)
}

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

  fun execute(
    identifier: Int?,
    sql: String,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): QueryResult<Long>

  fun <R> executeQuery(
    identifier: Int?,
    sql: String,
    mapper: (SqlCursor) -> QueryResult<R>,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): QueryResult<R>
}

class RealWasmoDbConnection(
  override val sqlConnection: SqlConnection,
) : WasmoDbConnection, WasmoDbTransaction {
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

  override fun afterCommit(function: () -> Unit) {
  }

  override fun close() {
    sqlConnection.close()
  }

  override fun execute(
    identifier: Int?,
    sql: String,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): QueryResult<Long> {
    return QueryResult.AsyncValue {
      sqlConnection.execute(
        sql,
        bindParameters = {
          binders?.invoke(RealSqlPreparedStatement(this))
        },
      )
    }
  }

  override fun <R> executeQuery(
    identifier: Int?,
    sql: String,
    mapper: (SqlCursor) -> QueryResult<R>,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): QueryResult<R> {
    return QueryResult.AsyncValue {
      val rowIterator = sqlConnection.executeQuery(
        sql,
        bindParameters = {
          binders?.invoke(RealSqlPreparedStatement(this))
        },
      )
      mapper(RealSqlCursor(rowIterator)).await()
    }
  }
}

class RealSqlCursor(
  private val rowIterator: RowIterator,
) : SqlCursor {
  private var currentRow: SqlRow? = null

  override fun next(): QueryResult<Boolean> {
    return QueryResult.AsyncValue {
      val next = rowIterator.next()
      currentRow = next
      return@AsyncValue next != null
    }
  }

  override fun getBoolean(index: Int): Boolean? {
    return currentRow!!.getBool(index)
  }

  override fun getBytes(index: Int): ByteArray? {
    return currentRow!!.getBytes(index)?.toByteArray()
  }

  override fun getDouble(index: Int): Double? {
    return currentRow!!.getF64(index)
  }

  override fun getLong(index: Int): Long? {
    return currentRow!!.getS64(index)
  }

  override fun getString(index: Int): String? {
    return currentRow!!.getString(index)
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

  override fun bindLong(index: Int, long: Long?) {
    sqlBinder.bindS64(index, long)
  }

  override fun bindString(index: Int, string: String?) {
    sqlBinder.bindString(index, string)
  }
}
