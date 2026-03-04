package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.jdbc.JdbcDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.wasmo.api.AppSlug
import com.wasmo.api.ComputerSlug
import com.wasmo.api.WasmoJson
import com.wasmo.db.Account
import com.wasmo.db.AppInstall
import com.wasmo.db.Computer
import com.wasmo.db.ComputerAccess
import com.wasmo.db.ComputerAllocation
import com.wasmo.db.ComputerSpec
import com.wasmo.db.Cookie
import com.wasmo.db.Invite
import com.wasmo.db.Passkey
import com.wasmo.db.StripeCustomer
import com.wasmo.db.WasmoDb
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.AppInstallId
import com.wasmo.identifiers.ComputerAccessId
import com.wasmo.identifiers.ComputerAllocationId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSpecId
import com.wasmo.identifiers.CookieId
import com.wasmo.identifiers.InviteId
import com.wasmo.identifiers.PasskeyId
import com.wasmo.identifiers.StripeCustomerId
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
) : Closeable by connectionPool, WasmoDb by WasmoDb.Companion(
  jdbcDriver,
  AccountAdapter,
  AppInstallAdapter,
  ComputerAccessAdapter,
  ComputerAdapter,
  ComputerAllocationAdapter,
  ComputerSpecAdapter,
  CookieAdapter,
  InviteAdapter,
  PasskeyAdapter,
  StripeCustomerAdapter,
) {
  fun clearSchema() {
    jdbcDriver.execute(null, "DROP SCHEMA public CASCADE", 0)
    jdbcDriver.execute(null, "CREATE SCHEMA public", 0)
    jdbcDriver.execute(null, "GRANT ALL ON SCHEMA public TO postgres", 0)
    jdbcDriver.execute(null, "GRANT ALL ON SCHEMA public TO public", 0)
  }

  fun migrate(
    oldVersion: Long = 0L,
    newVersion: Long = WasmoDb.Schema.version,
  ) {
    WasmoDb.Schema.migrate(jdbcDriver, oldVersion, newVersion)
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

    private object AppSlugAdapter : ColumnAdapter<AppSlug, String> {
      override fun decode(databaseValue: String) = AppSlug(databaseValue)
      override fun encode(value: AppSlug) = value.value
    }

    private object ComputerAllocationIdAdapter : ColumnAdapter<ComputerAllocationId, Long> {
      override fun decode(databaseValue: Long) = ComputerAllocationId(databaseValue)
      override fun encode(value: ComputerAllocationId) = value.id
    }

    private object ComputerAccessIdAdapter : ColumnAdapter<ComputerAccessId, Long> {
      override fun decode(databaseValue: Long) = ComputerAccessId(databaseValue)
      override fun encode(value: ComputerAccessId) = value.id
    }

    private object ComputerIdAdapter : ColumnAdapter<ComputerId, Long> {
      override fun decode(databaseValue: Long) = ComputerId(databaseValue)
      override fun encode(value: ComputerId) = value.id
    }

    private object ComputerSlugAdapter : ColumnAdapter<ComputerSlug, String> {
      override fun decode(databaseValue: String) = ComputerSlug(databaseValue)
      override fun encode(value: ComputerSlug) = value.value
    }

    private object ComputerSpecIdAdapter : ColumnAdapter<ComputerSpecId, Long> {
      override fun decode(databaseValue: Long) = ComputerSpecId(databaseValue)
      override fun encode(value: ComputerSpecId) = value.id
    }

    private object CookieIdAdapter : ColumnAdapter<CookieId, Long> {
      override fun decode(databaseValue: Long) = CookieId(databaseValue)
      override fun encode(value: CookieId) = value.id
    }

    private object InviteIdAdapter : ColumnAdapter<InviteId, Long> {
      override fun decode(databaseValue: Long) = InviteId(databaseValue)
      override fun encode(value: InviteId) = value.id
    }

    private object PasskeyIdAdapter : ColumnAdapter<PasskeyId, Long> {
      override fun decode(databaseValue: Long) = PasskeyId(databaseValue)
      override fun encode(value: PasskeyId) = value.id
    }

    private object StripeCustomerIdAdapter : ColumnAdapter<StripeCustomerId, Long> {
      override fun decode(databaseValue: Long) = StripeCustomerId(databaseValue)
      override fun encode(value: StripeCustomerId) = value.id
    }

    private val AccountAdapter = Account.Adapter(
      idAdapter = AccountIdAdapter,
    )

    private val AppInstallAdapter = AppInstall.Adapter(
      idAdapter = AppInstallIdAdapter,
      computer_idAdapter = ComputerIdAdapter,
      slugAdapter = AppSlugAdapter,
      install_scheduled_atAdapter = InstantAdapter,
      install_completed_atAdapter = InstantAdapter,
      install_deleted_atAdapter = InstantAdapter,
    )

    private val ComputerAdapter = Computer.Adapter(
      idAdapter = ComputerIdAdapter,
      created_atAdapter = InstantAdapter,
      slugAdapter = ComputerSlugAdapter,
    )

    private val ComputerAccessAdapter = ComputerAccess.Adapter(
      idAdapter = ComputerAccessIdAdapter,
      created_atAdapter = InstantAdapter,
      computer_idAdapter = ComputerIdAdapter,
      account_idAdapter = AccountIdAdapter,
    )

    private val ComputerAllocationAdapter = ComputerAllocation.Adapter(
      idAdapter = ComputerAllocationIdAdapter,
      created_atAdapter = InstantAdapter,
      stripe_customer_idAdapter = StripeCustomerIdAdapter,
      computer_idAdapter = ComputerIdAdapter,
      active_startAdapter = InstantAdapter,
      active_endAdapter = InstantAdapter,
    )

    private val ComputerSpecAdapter = ComputerSpec.Adapter(
      idAdapter = ComputerSpecIdAdapter,
      created_atAdapter = InstantAdapter,
      account_idAdapter = AccountIdAdapter,
      computer_idAdapter = ComputerIdAdapter,
      slugAdapter = ComputerSlugAdapter,
    )

    private val CookieAdapter = Cookie.Adapter(
      idAdapter = CookieIdAdapter,
      created_atAdapter = InstantAdapter,
      account_idAdapter = AccountIdAdapter,
    )

    private val InviteAdapter = Invite.Adapter(
      idAdapter = InviteIdAdapter,
      created_atAdapter = InstantAdapter,
      created_byAdapter = AccountIdAdapter,
      claimed_atAdapter = InstantAdapter,
      claimed_byAdapter = AccountIdAdapter,
    )

    private val PasskeyAdapter = Passkey.Adapter(
      idAdapter = PasskeyIdAdapter,
      created_atAdapter = InstantAdapter,
      account_idAdapter = AccountIdAdapter,
      registration_recordAdapter = RegistrationRecordAdapter,
    )

    private val StripeCustomerAdapter = StripeCustomer.Adapter(
      idAdapter = StripeCustomerIdAdapter,
      created_atAdapter = InstantAdapter,
    )
  }
}
