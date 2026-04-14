package com.wasmo.app.db

import app.cash.sqldelight.db.QueryResult
import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.PasskeyId
import com.wasmo.passkeys.RegistrationRecord
import java.time.OffsetDateTime
import kotlin.time.Instant

public class PasskeyQueries(
  private val driver: SqlDriver,
  private val PasskeyAdapter: Passkey.Adapter,
) {
  public fun <T : Any> findPasskeyByPasskeyId(passkey_id: String, mapper: (
    id: PasskeyId,
    created_at: Instant,
    account_id: AccountId,
    passkey_id: String,
    aaguid: String,
    created_by_user_agent: String?,
    created_by_ip: String?,
    registration_record: RegistrationRecord,
  ) -> T): Query<T> = FindPasskeyByPasskeyIdQuery(passkey_id) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      PasskeyAdapter.idAdapter.decode(cursor.getLong(0)!!),
      PasskeyAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      PasskeyAdapter.account_idAdapter.decode(cursor.getLong(2)!!),
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5),
      cursor.getString(6),
      PasskeyAdapter.registration_recordAdapter.decode(cursor.getString(7)!!)
    )
  }

  public fun findPasskeyByPasskeyId(passkey_id: String): Query<Passkey> = findPasskeyByPasskeyId(passkey_id, ::Passkey)

  public fun <T : Any> findPasskeyByPasskeyIdAndAccountId(
    passkey_id: String,
    account_id: AccountId,
    mapper: (
      id: PasskeyId,
      created_at: Instant,
      account_id: AccountId,
      passkey_id: String,
      aaguid: String,
      created_by_user_agent: String?,
      created_by_ip: String?,
      registration_record: RegistrationRecord,
    ) -> T,
  ): Query<T> = FindPasskeyByPasskeyIdAndAccountIdQuery(passkey_id, account_id) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      PasskeyAdapter.idAdapter.decode(cursor.getLong(0)!!),
      PasskeyAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      PasskeyAdapter.account_idAdapter.decode(cursor.getLong(2)!!),
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5),
      cursor.getString(6),
      PasskeyAdapter.registration_recordAdapter.decode(cursor.getString(7)!!)
    )
  }

  public fun findPasskeyByPasskeyIdAndAccountId(passkey_id: String, account_id: AccountId): Query<Passkey> = findPasskeyByPasskeyIdAndAccountId(passkey_id, account_id, ::Passkey)

  public fun <T : Any> findPasskeysByAccountId(account_id: AccountId, mapper: (
    id: PasskeyId,
    created_at: Instant,
    account_id: AccountId,
    passkey_id: String,
    aaguid: String,
    created_by_user_agent: String?,
    created_by_ip: String?,
    registration_record: RegistrationRecord,
  ) -> T): Query<T> = FindPasskeysByAccountIdQuery(account_id) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      PasskeyAdapter.idAdapter.decode(cursor.getLong(0)!!),
      PasskeyAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      PasskeyAdapter.account_idAdapter.decode(cursor.getLong(2)!!),
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5),
      cursor.getString(6),
      PasskeyAdapter.registration_recordAdapter.decode(cursor.getString(7)!!)
    )
  }

  public fun findPasskeysByAccountId(account_id: AccountId): Query<Passkey> = findPasskeysByAccountId(account_id, ::Passkey)

  /**
   * @return The number of rows updated.
   */
  public suspend fun insertPasskey(
    created_at: Instant,
    account_id: AccountId,
    passkey_id: String,
    aaguid: String,
    created_by_user_agent: String?,
    created_by_ip: String?,
    registration_record: RegistrationRecord,
  ): Long {
    val result = driver.execute(-145_919_451, """
        |INSERT INTO Passkey(
        |  created_at,
        |  account_id,
        |  passkey_id,
        |  aaguid,
        |  created_by_user_agent,
        |  created_by_ip,
        |  registration_record
        |)
        |VALUES (
        |  ?,
        |  ?,
        |  ?,
        |  ?,
        |  ?,
        |  ?,
        |  ?
        |)
        """.trimMargin(), 7) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindObject(parameterIndex++, PasskeyAdapter.created_atAdapter.encode(created_at))
          bindLong(parameterIndex++, PasskeyAdapter.account_idAdapter.encode(account_id))
          bindString(parameterIndex++, passkey_id)
          bindString(parameterIndex++, aaguid)
          bindString(parameterIndex++, created_by_user_agent)
          bindString(parameterIndex++, created_by_ip)
          bindString(parameterIndex++, PasskeyAdapter.registration_recordAdapter.encode(registration_record))
        }
    return result
  }

  private inner class FindPasskeyByPasskeyIdQuery<out T : Any>(
    public val passkey_id: String,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(303_956_845, """
    |SELECT Passkey.id, Passkey.created_at, Passkey.account_id, Passkey.passkey_id, Passkey.aaguid, Passkey.created_by_user_agent, Passkey.created_by_ip, Passkey.registration_record
    |FROM Passkey
    |WHERE
    |  passkey_id = ?
    """.trimMargin(), mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindString(parameterIndex++, passkey_id)
    }

    override fun toString(): String = "Passkey.sq:findPasskeyByPasskeyId"
  }

  private inner class FindPasskeyByPasskeyIdAndAccountIdQuery<out T : Any>(
    public val passkey_id: String,
    public val account_id: AccountId,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(1_368_858_654, """
    |SELECT Passkey.id, Passkey.created_at, Passkey.account_id, Passkey.passkey_id, Passkey.aaguid, Passkey.created_by_user_agent, Passkey.created_by_ip, Passkey.registration_record FROM Passkey
    |WHERE
    |   passkey_id = ? AND
    |   account_id = ?
    """.trimMargin(), mapper, 2) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindString(parameterIndex++, passkey_id)
      bindLong(parameterIndex++, PasskeyAdapter.account_idAdapter.encode(account_id))
    }

    override fun toString(): String = "Passkey.sq:findPasskeyByPasskeyIdAndAccountId"
  }

  private inner class FindPasskeysByAccountIdQuery<out T : Any>(
    public val account_id: AccountId,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(2_146_426_563, """SELECT Passkey.id, Passkey.created_at, Passkey.account_id, Passkey.passkey_id, Passkey.aaguid, Passkey.created_by_user_agent, Passkey.created_by_ip, Passkey.registration_record FROM Passkey WHERE account_id = ?""", mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, PasskeyAdapter.account_idAdapter.encode(account_id))
    }

    override fun toString(): String = "Passkey.sq:findPasskeysByAccountId"
  }
}
