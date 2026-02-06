package app.rounds.app.db

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.jdbc.JdbcDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import app.rounds.game.identifiers.ComputerId
import com.publicobject.wasmcomputer.Computer
import com.publicobject.wasmcomputer.WasmComputerDb
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

class RoundsDbService(
  val connectionPool: ObjectPool<PoolableConnection>,
  val jdbcDriver: JdbcDriver,
) : Closeable by connectionPool, WasmComputerDb by WasmComputerDb.Companion(
  jdbcDriver,
  ComputerAdapter,
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
    ): RoundsDbService {
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

      return RoundsDbService(
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

    private object ComputerIdAdapter : ColumnAdapter<ComputerId, Long> {
      override fun decode(databaseValue: Long) = ComputerId(databaseValue)
      override fun encode(value: ComputerId) = value.id
    }

    private val ComputerAdapter = Computer.Adapter(
      idAdapter = ComputerIdAdapter,
      created_atAdapter = InstantAdapter,
    )
  }
}
