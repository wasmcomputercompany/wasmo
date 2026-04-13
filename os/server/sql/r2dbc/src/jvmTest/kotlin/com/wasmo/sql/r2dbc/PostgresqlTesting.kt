package com.wasmo.sql.r2dbc

import io.r2dbc.postgresql.api.PostgresqlConnection as Connection
import io.r2dbc.postgresql.api.PostgresqlStatement
import io.r2dbc.spi.Readable
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle

internal suspend fun PostgresqlStatement.rowCount(): Long =
  execute().awaitSingle().rowsUpdated.awaitSingle()

internal suspend inline fun Connection.execute(
  sql: String,
): Long = createStatement(sql).rowCount()

internal suspend inline fun <reified P0> Connection.execute(
  sql: String,
  p0: P0,
): Long = createStatement(sql).run {
  bindNullable(0, p0 ?: P0::class.java)
  rowCount()
}

internal suspend inline fun <reified P0, reified P1> Connection.execute(
  sql: String,
  p0: P0,
  p1: P1,
): Long = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  rowCount()
}

internal suspend inline fun <reified P0, reified P1, reified P2> Connection.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
): Long = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  rowCount()
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3> Connection.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
): Long = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  bindNullable(3, p3)
  rowCount()
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, reified P4> Connection.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
): Long = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  bindNullable(3, p3)
  bindNullable(4, p4)
  rowCount()
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, reified P4, reified P5> Connection.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
  p5: P5,
): Long = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  bindNullable(3, p3)
  bindNullable(4, p4)
  bindNullable(5, p5)
  rowCount()
}

internal suspend inline fun <R : Any> Connection.executeQuery(
  sql: String,
  noinline rowMapper: Readable.() -> R,
): List<R> = createStatement(sql).rows(rowMapper)

internal suspend fun <R : Any> PostgresqlStatement.rows(
  rowMapper: Readable.() -> R,
): List<R> = execute().awaitSingle().map(rowMapper).asFlow().toList()


internal suspend inline fun <reified P0, R : Any> Connection.executeQuery(
  sql: String,
  p0: P0,
  noinline rowMapper: Readable.() -> R,
): List<R> = createStatement(sql).run {
  bindNullable(0, p0 ?: P0::class.java)
  rows(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, R : Any> Connection.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  noinline rowMapper: io.r2dbc.spi.Readable.() -> R,
): List<R> = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  rows(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, reified P2, R : Any> Connection.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  noinline rowMapper: io.r2dbc.spi.Readable.() -> R,
): List<R> = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  rows(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, R : Any> Connection.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  noinline rowMapper: io.r2dbc.spi.Readable.() -> R,
): List<R> = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  bindNullable(3, p3)
  rows(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, reified P4, R : Any> Connection.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
  noinline rowMapper: io.r2dbc.spi.Readable.() -> R,
): List<R> = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  bindNullable(3, p3)
  bindNullable(4, p4)
  rows(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, reified P4, reified P5, R : Any> Connection.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
  p5: P5,
  noinline rowMapper: Readable.() -> R,
): List<R> = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  bindNullable(3, p3)
  bindNullable(4, p4)
  bindNullable(5, p5)
  rows(rowMapper)
}

internal inline fun <reified T> PostgresqlStatement.bindNullable(index: Int, value: T?) {
  when {
    value != null -> bind(index, value)
    else -> bindNull(index, T::class.java)
  }
}
