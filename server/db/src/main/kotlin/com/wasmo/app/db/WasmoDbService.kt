package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.jdbc.JdbcDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.wasmo.api.WasmoJson
import com.wasmo.db.Account
import com.wasmo.db.AppInstall
import com.wasmo.db.Computer
import com.wasmo.db.Cookie
import com.wasmo.db.Passkey
import com.wasmo.db.WasmComputerDb
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.AppInstallId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.CookieId
import com.wasmo.identifiers.PasskeyId
import com.wasmo.passkeys.RegistrationRecord
import java.io.Closeable
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Properties
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant
import org.apache.commons.dbcp2.DriverManagerConnectionFactory
import org.apache.commons.dbcp2.PoolableConnection
import org.apache.commons.dbcp2.PoolableConnectionFactory
import org.apache.commons.dbcp2.PoolingDataSource
import org.apache.commons.pool2.ObjectPool
import org.apache.commons.pool2.impl.GenericObjectPool

class WasmoDbService(
  val connectionPool: ObjectPool<PoolableConnection>,
  val jdbcDriver: JdbcDriver,
) : Closeable by connectionPool, WasmComputerDb by WasmComputerDb.Companion(
  jdbcDriver,
  AccountAdapter,
  AppInstallAdapter,
  ComputerAdapter,
  CookieAdapter,
  PasskeyAdapter,
) {
  fun clearSchema() {
    jdbcDriver.execute(null, "DROP SCHEMA public CASCADE", 0)
    jdbcDriver.execute(null, "CREATE SCHEMA public", 0)
    jdbcDriver.execute(null, "GRANT ALL ON SCHEMA public TO postgres", 0)
    jdbcDriver.execute(null, "GRANT ALL ON SCHEMA public TO public", 0)
  }

  fun migrate(
    oldVersion: Long = 0L,
    newVersion: Long = WasmComputerDb.Schema.version,
  ) {
    WasmComputerDb.Schema.migrate(jdbcDriver, oldVersion, newVersion)
  }

  companion object {
    fun start(
      hostname: String,
      databaseName: String,
      user: String,
      password: String,
      ssl: Boolean,
    ): WasmoDbService {
      val connectUri = "jdbc:postgresql://${hostname}/${databaseName}"
      val properties = Properties().apply {
        setProperty("user", user)
        setProperty("password", password)
        setProperty("ssl", ssl.toString())
      }

      val connectionFactory = PoolableConnectionFactory(
        DriverManagerConnectionFactory(connectUri, properties),
        null,
      )

      val connectionPool = GenericObjectPool(connectionFactory)
      connectionFactory.pool = connectionPool

      val dataSource = PoolingDataSource(connectionPool)
      val jdbcDriver = dataSource.asJdbcDriver()

      return WasmoDbService(
        connectionPool = connectionPool,
        jdbcDriver = jdbcDriver,
      )
    }

    private object InstantAdapter : ColumnAdapter<Instant, OffsetDateTime> {
      override fun decode(databaseValue: OffsetDateTime) =
        databaseValue.toInstant().toKotlinInstant()

      override fun encode(value: Instant) =
        value.toJavaInstant().atOffset(ZoneOffset.UTC)
    }

    private object RegistrationRecordAdapter : ColumnAdapter<RegistrationRecord, String> {
      override fun decode(databaseValue: String) =
        WasmoJson.decodeFromString<RegistrationRecord>(databaseValue)

      override fun encode(value: RegistrationRecord) =
        WasmoJson.encodeToString(value)
    }

    private object AccountIdAdapter : ColumnAdapter<AccountId, Long> {
      override fun decode(databaseValue: Long) = AccountId(databaseValue)
      override fun encode(value: AccountId) = value.id
    }

    private object AppInstallIdAdapter : ColumnAdapter<AppInstallId, Long> {
      override fun decode(databaseValue: Long) = AppInstallId(databaseValue)
      override fun encode(value: AppInstallId) = value.id
    }

    private object CookieIdAdapter : ColumnAdapter<CookieId, Long> {
      override fun decode(databaseValue: Long) = CookieId(databaseValue)
      override fun encode(value: CookieId) = value.id
    }

    private object ComputerIdAdapter : ColumnAdapter<ComputerId, Long> {
      override fun decode(databaseValue: Long) = ComputerId(databaseValue)
      override fun encode(value: ComputerId) = value.id
    }

    private object PasskeyIdAdapter : ColumnAdapter<PasskeyId, Long> {
      override fun decode(databaseValue: Long) = PasskeyId(databaseValue)
      override fun encode(value: PasskeyId) = value.id
    }

    private val AccountAdapter = Account.Adapter(
      idAdapter = AccountIdAdapter,
    )

    private val AppInstallAdapter = AppInstall.Adapter(
      idAdapter = AppInstallIdAdapter,
      created_atAdapter = InstantAdapter,
      computer_idAdapter = ComputerIdAdapter,
    )

    private val CookieAdapter = Cookie.Adapter(
      idAdapter = CookieIdAdapter,
      created_atAdapter = InstantAdapter,
      account_idAdapter = AccountIdAdapter,
    )

    private val ComputerAdapter = Computer.Adapter(
      idAdapter = ComputerIdAdapter,
      created_atAdapter = InstantAdapter,
    )

    private val PasskeyAdapter = Passkey.Adapter(
      idAdapter = PasskeyIdAdapter,
      created_atAdapter = InstantAdapter,
      account_idAdapter = AccountIdAdapter,
      registration_recordAdapter = RegistrationRecordAdapter,
    )

  }
}
