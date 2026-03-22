package com.wasmo.sql.jdbc

import com.wasmo.sql.PostgresqlAddress
import java.util.Properties
import org.apache.commons.dbcp2.DriverManagerConnectionFactory
import org.apache.commons.dbcp2.PoolableConnection
import org.apache.commons.dbcp2.PoolableConnectionFactory
import org.apache.commons.dbcp2.PoolingDataSource
import org.apache.commons.pool2.impl.GenericObjectPool

fun connectPostgresql(
  address: PostgresqlAddress,
): PoolingDataSource<PoolableConnection> {
  val connectUri = "jdbc:postgresql://${address.hostname}/${address.databaseName}"
  val properties = Properties().apply {
    setProperty("user", address.user)
    setProperty("password", address.password)
    setProperty("ssl", address.ssl.toString())
  }

  val connectionFactory = PoolableConnectionFactory(
    DriverManagerConnectionFactory(connectUri, properties),
    null,
  )

  val connectionPool = GenericObjectPool(connectionFactory)
  connectionFactory.pool = connectionPool

  return PoolingDataSource(connectionPool)
}
