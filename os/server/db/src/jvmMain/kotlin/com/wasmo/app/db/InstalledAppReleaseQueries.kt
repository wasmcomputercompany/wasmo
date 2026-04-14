package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import com.wasmo.app.db2.RealSqlCursor as JdbcCursor
import com.wasmo.app.db2.RealSqlPreparedStatement as JdbcPreparedStatement
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as ExecutableQuery
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.packaging.AppManifest
import kotlin.time.Instant

public class InstalledAppReleaseQueries(
  private val driver: SqlDriver,
  private val InstalledAppReleaseAdapter: InstalledAppRelease.Adapter,
) {
  public fun insertInstalledAppRelease(
    first_active_at: Instant,
    computer_id: ComputerId,
    installed_app_id: InstalledAppId,
    app_version: Long,
    app_manifest_data: AppManifest,
  ): ExecutableQuery<InstalledAppReleaseId> = InsertInstalledAppReleaseQuery(first_active_at, computer_id, installed_app_id, app_version, app_manifest_data) { cursor ->
    check(cursor is JdbcCursor)
    InstalledAppReleaseAdapter.idAdapter.decode(cursor.getS64(0)!!)
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
      InstalledAppReleaseAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      InstalledAppReleaseAdapter.computer_idAdapter.decode(cursor.getS64(2)!!),
      InstalledAppReleaseAdapter.installed_app_idAdapter.decode(cursor.getS64(3)!!),
      cursor.getS64(4)!!,
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
    mapper: suspend (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(-935_160_091, """
    |INSERT INTO InstalledAppRelease(
    |  first_active_at,
    |  computer_id,
    |  installed_app_id,
    |  app_version,
    |  app_manifest_data
    |)
    |VALUES (
    |  $1,
    |  $2,
    |  $3,
    |  $4,
    |  $5
    |) RETURNING id
    """.trimMargin(), mapper, 5) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindInstant(parameterIndex++, first_active_at)
      bindLong(parameterIndex++, InstalledAppReleaseAdapter.computer_idAdapter.encode(computer_id))
      bindLong(parameterIndex++, InstalledAppReleaseAdapter.installed_app_idAdapter.encode(installed_app_id))
      bindLong(parameterIndex++, app_version)
      bindString(parameterIndex++, InstalledAppReleaseAdapter.app_manifest_dataAdapter.encode(app_manifest_data))
    }

    override fun toString(): String = "InstalledAppRelease.sq:insertInstalledAppRelease"
  }

  private inner class SelectInstalledAppReleaseByIdQuery<out T : Any>(
    public val id: InstalledAppReleaseId,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(-75_751_756, """
    |SELECT InstalledAppRelease.id, InstalledAppRelease.first_active_at, InstalledAppRelease.computer_id, InstalledAppRelease.installed_app_id, InstalledAppRelease.app_version, InstalledAppRelease.app_manifest_data FROM InstalledAppRelease
    |WHERE id = $1
    |LIMIT 1
    """.trimMargin(), mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, InstalledAppReleaseAdapter.idAdapter.encode(id))
    }

    override fun toString(): String = "InstalledAppRelease.sq:selectInstalledAppReleaseById"
  }
}
