package com.wasmo.app.db

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.packaging.AppManifest
import java.time.OffsetDateTime
import kotlin.Any
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

public class InstalledAppReleaseQueries(
  driver: SqlDriver,
  private val InstalledAppReleaseAdapter: InstalledAppRelease.Adapter,
) : TransacterImpl(driver) {
  public fun insertInstalledAppRelease(
    first_active_at: Instant,
    computer_id: ComputerId,
    installed_app_id: InstalledAppId,
    app_version: Long,
    app_manifest_data: AppManifest,
  ): ExecutableQuery<InstalledAppReleaseId> = InsertInstalledAppReleaseQuery(first_active_at, computer_id, installed_app_id, app_version, app_manifest_data) { cursor ->
    check(cursor is JdbcCursor)
    InstalledAppReleaseAdapter.idAdapter.decode(cursor.getLong(0)!!)
  }

  public fun <T : Any> selectInstalledAppReleaseById(id: InstalledAppReleaseId, mapper: (
    id: InstalledAppReleaseId,
    first_active_at: Instant,
    computer_id: ComputerId,
    installed_app_id: InstalledAppId,
    app_version: Long,
    app_manifest_data: AppManifest,
  ) -> T): Query<T> = SelectInstalledAppReleaseByIdQuery(id) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      InstalledAppReleaseAdapter.idAdapter.decode(cursor.getLong(0)!!),
      InstalledAppReleaseAdapter.first_active_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      InstalledAppReleaseAdapter.computer_idAdapter.decode(cursor.getLong(2)!!),
      InstalledAppReleaseAdapter.installed_app_idAdapter.decode(cursor.getLong(3)!!),
      cursor.getLong(4)!!,
      InstalledAppReleaseAdapter.app_manifest_dataAdapter.decode(cursor.getString(5)!!)
    )
  }

  public fun selectInstalledAppReleaseById(id: InstalledAppReleaseId): Query<InstalledAppRelease> = selectInstalledAppReleaseById(id, ::InstalledAppRelease)

  private inner class InsertInstalledAppReleaseQuery<out T : Any>(
    public val first_active_at: Instant,
    public val computer_id: ComputerId,
    public val installed_app_id: InstalledAppId,
    public val app_version: Long,
    public val app_manifest_data: AppManifest,
    mapper: (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-935_160_091, """
    |INSERT INTO InstalledAppRelease(
    |  first_active_at,
    |  computer_id,
    |  installed_app_id,
    |  app_version,
    |  app_manifest_data
    |)
    |VALUES (
    |  ?,
    |  ?,
    |  ?,
    |  ?,
    |  ?
    |) RETURNING id
    """.trimMargin(), mapper, 5) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindObject(parameterIndex++, InstalledAppReleaseAdapter.first_active_atAdapter.encode(first_active_at))
      bindLong(parameterIndex++, InstalledAppReleaseAdapter.computer_idAdapter.encode(computer_id))
      bindLong(parameterIndex++, InstalledAppReleaseAdapter.installed_app_idAdapter.encode(installed_app_id))
      bindLong(parameterIndex++, app_version)
      bindString(parameterIndex++, InstalledAppReleaseAdapter.app_manifest_dataAdapter.encode(app_manifest_data))
    }.also {
      notifyQueries(-935_160_091) { emit ->
        emit("InstalledAppRelease")
      }
    }

    override fun toString(): String = "InstalledAppRelease.sq:insertInstalledAppRelease"
  }

  private inner class SelectInstalledAppReleaseByIdQuery<out T : Any>(
    public val id: InstalledAppReleaseId,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Listener) {
      driver.addListener("InstalledAppRelease", listener = listener)
    }

    override fun removeListener(listener: Listener) {
      driver.removeListener("InstalledAppRelease", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-75_751_756, """
    |SELECT InstalledAppRelease.id, InstalledAppRelease.first_active_at, InstalledAppRelease.computer_id, InstalledAppRelease.installed_app_id, InstalledAppRelease.app_version, InstalledAppRelease.app_manifest_data FROM InstalledAppRelease
    |WHERE id = ?
    |LIMIT 1
    """.trimMargin(), mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, InstalledAppReleaseAdapter.idAdapter.encode(id))
    }

    override fun toString(): String = "InstalledAppRelease.sq:selectInstalledAppReleaseById"
  }
}
