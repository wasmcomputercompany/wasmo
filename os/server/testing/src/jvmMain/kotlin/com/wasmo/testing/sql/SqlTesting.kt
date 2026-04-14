package com.wasmo.testing.sql

import org.apache.commons.dbcp2.PoolableConnection
import org.apache.commons.dbcp2.PoolingDataSource

fun PoolingDataSource<PoolableConnection>.clearSchema() {
  connection.use { connection ->
    connection.prepareStatement("DROP SCHEMA IF EXISTS public CASCADE").executeUpdate()
    connection.prepareStatement("CREATE SCHEMA public").executeUpdate()
    connection.prepareStatement("GRANT ALL ON SCHEMA public TO postgres").executeUpdate()
    connection.prepareStatement("GRANT ALL ON SCHEMA public TO public").executeUpdate()
  }
}
