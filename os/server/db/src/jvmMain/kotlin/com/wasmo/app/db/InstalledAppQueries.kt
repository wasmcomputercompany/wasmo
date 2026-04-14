package com.wasmo.app.db

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import java.time.OffsetDateTime
import kotlin.Any
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

public class InstalledAppQueries(
  driver: SqlDriver,
  private val InstalledAppAdapter: InstalledApp.Adapter,
  private val InstalledAppReleaseAdapter: InstalledAppRelease.Adapter,
) : TransacterImpl(driver) {
  public fun insertInstalledApp(
    installed_at: Instant,
    computer_id: ComputerId,
    slug: AppSlug,
    active: Boolean?,
    version: Long,
    wasmo_file_address: WasmoFileAddress,
  ): ExecutableQuery<InstalledAppId> = InsertInstalledAppQuery(installed_at, computer_id, slug, active, version, wasmo_file_address) { cursor ->
    check(cursor is JdbcCursor)
    InstalledAppAdapter.idAdapter.decode(cursor.getLong(0)!!)
  }

  public fun <T : Any> selectInstalledAppsByComputerId(
    computer_id: ComputerId,
    active: Boolean?,
    limit: Long,
    mapper: (
      id: InstalledAppId,
      installed_at: Instant,
      computer_id: ComputerId,
      slug: AppSlug,
      active: Boolean?,
      version: Long,
      wasmo_file_address: WasmoFileAddress,
      active_release_id: InstalledAppReleaseId?,
      id_: InstalledAppReleaseId?,
      first_active_at: Instant?,
      computer_id_: ComputerId?,
      installed_app_id: InstalledAppId?,
      app_version: Long?,
      app_manifest_data: AppManifest?,
    ) -> T,
  ): Query<T> = SelectInstalledAppsByComputerIdQuery(computer_id, active, limit) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      InstalledAppAdapter.idAdapter.decode(cursor.getLong(0)!!),
      InstalledAppAdapter.installed_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      InstalledAppAdapter.computer_idAdapter.decode(cursor.getLong(2)!!),
      InstalledAppAdapter.slugAdapter.decode(cursor.getString(3)!!),
      cursor.getBoolean(4),
      cursor.getLong(5)!!,
      InstalledAppAdapter.wasmo_file_addressAdapter.decode(cursor.getString(6)!!),
      cursor.getLong(7)?.let { InstalledAppAdapter.active_release_idAdapter.decode(it) },
      cursor.getLong(8)?.let { InstalledAppReleaseAdapter.idAdapter.decode(it) },
      cursor.getObject<OffsetDateTime>(9)?.let { InstalledAppReleaseAdapter.first_active_atAdapter.decode(it) },
      cursor.getLong(10)?.let { InstalledAppReleaseAdapter.computer_idAdapter.decode(it) },
      cursor.getLong(11)?.let { InstalledAppReleaseAdapter.installed_app_idAdapter.decode(it) },
      cursor.getLong(12),
      cursor.getString(13)?.let { InstalledAppReleaseAdapter.app_manifest_dataAdapter.decode(it) }
    )
  }

  public fun selectInstalledAppsByComputerId(
    computer_id: ComputerId,
    active: Boolean?,
    limit: Long,
  ): Query<SelectInstalledAppsByComputerId> = selectInstalledAppsByComputerId(computer_id, active, limit, ::SelectInstalledAppsByComputerId)

  public fun <T : Any> selectInstalledAppByComputerIdAndSlug(
    computer_id: ComputerId,
    slug: AppSlug,
    active: Boolean?,
    mapper: (
      id: InstalledAppId,
      installed_at: Instant,
      computer_id: ComputerId,
      slug: AppSlug,
      active: Boolean?,
      version: Long,
      wasmo_file_address: WasmoFileAddress,
      active_release_id: InstalledAppReleaseId?,
      id_: InstalledAppReleaseId?,
      first_active_at: Instant?,
      computer_id_: ComputerId?,
      installed_app_id: InstalledAppId?,
      app_version: Long?,
      app_manifest_data: AppManifest?,
    ) -> T,
  ): Query<T> = SelectInstalledAppByComputerIdAndSlugQuery(computer_id, slug, active) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      InstalledAppAdapter.idAdapter.decode(cursor.getLong(0)!!),
      InstalledAppAdapter.installed_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      InstalledAppAdapter.computer_idAdapter.decode(cursor.getLong(2)!!),
      InstalledAppAdapter.slugAdapter.decode(cursor.getString(3)!!),
      cursor.getBoolean(4),
      cursor.getLong(5)!!,
      InstalledAppAdapter.wasmo_file_addressAdapter.decode(cursor.getString(6)!!),
      cursor.getLong(7)?.let { InstalledAppAdapter.active_release_idAdapter.decode(it) },
      cursor.getLong(8)?.let { InstalledAppReleaseAdapter.idAdapter.decode(it) },
      cursor.getObject<OffsetDateTime>(9)?.let { InstalledAppReleaseAdapter.first_active_atAdapter.decode(it) },
      cursor.getLong(10)?.let { InstalledAppReleaseAdapter.computer_idAdapter.decode(it) },
      cursor.getLong(11)?.let { InstalledAppReleaseAdapter.installed_app_idAdapter.decode(it) },
      cursor.getLong(12),
      cursor.getString(13)?.let { InstalledAppReleaseAdapter.app_manifest_dataAdapter.decode(it) }
    )
  }

  public fun selectInstalledAppByComputerIdAndSlug(
    computer_id: ComputerId,
    slug: AppSlug,
    active: Boolean?,
  ): Query<SelectInstalledAppByComputerIdAndSlug> = selectInstalledAppByComputerIdAndSlug(computer_id, slug, active, ::SelectInstalledAppByComputerIdAndSlug)

  public fun <T : Any> selectInstalledAppById(id: InstalledAppId, mapper: (
    id: InstalledAppId,
    installed_at: Instant,
    computer_id: ComputerId,
    slug: AppSlug,
    active: Boolean?,
    version: Long,
    wasmo_file_address: WasmoFileAddress,
    active_release_id: InstalledAppReleaseId?,
  ) -> T): Query<T> = SelectInstalledAppByIdQuery(id) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      InstalledAppAdapter.idAdapter.decode(cursor.getLong(0)!!),
      InstalledAppAdapter.installed_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      InstalledAppAdapter.computer_idAdapter.decode(cursor.getLong(2)!!),
      InstalledAppAdapter.slugAdapter.decode(cursor.getString(3)!!),
      cursor.getBoolean(4),
      cursor.getLong(5)!!,
      InstalledAppAdapter.wasmo_file_addressAdapter.decode(cursor.getString(6)!!),
      cursor.getLong(7)?.let { InstalledAppAdapter.active_release_idAdapter.decode(it) }
    )
  }

  public fun selectInstalledAppById(id: InstalledAppId): Query<InstalledApp> = selectInstalledAppById(id, ::InstalledApp)

  /**
   * @return The number of rows updated.
   */
  public fun setRelease(
    new_version: Long,
    active_release_id: InstalledAppReleaseId?,
    expected_version: Long,
    id: InstalledAppId,
  ): QueryResult<Long> {
    val result = driver.execute(-1_970_953_018, """
        |UPDATE InstalledApp
        |SET
        |  version = ?,
        |  active_release_id = ?
        |WHERE
        |  version = ? AND
        |  id = ?
        """.trimMargin(), 4) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindLong(parameterIndex++, new_version)
          bindLong(parameterIndex++, active_release_id?.let { InstalledAppAdapter.active_release_idAdapter.encode(it) })
          bindLong(parameterIndex++, expected_version)
          bindLong(parameterIndex++, InstalledAppAdapter.idAdapter.encode(id))
        }
    notifyQueries(-1_970_953_018) { emit ->
      emit("InstalledApp")
    }
    return result
  }

  private inner class InsertInstalledAppQuery<out T : Any>(
    public val installed_at: Instant,
    public val computer_id: ComputerId,
    public val slug: AppSlug,
    public val active: Boolean?,
    public val version: Long,
    public val wasmo_file_address: WasmoFileAddress,
    mapper: (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_804_962_849, """
    |INSERT INTO InstalledApp(
    |  installed_at,
    |  computer_id,
    |  slug,
    |  active,
    |  version,
    |  wasmo_file_address
    |)
    |VALUES (
    |  ?,
    |  ?,
    |  ?,
    |  ?,
    |  ?,
    |  ?
    |) RETURNING id
    """.trimMargin(), mapper, 6) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindObject(parameterIndex++, InstalledAppAdapter.installed_atAdapter.encode(installed_at))
      bindLong(parameterIndex++, InstalledAppAdapter.computer_idAdapter.encode(computer_id))
      bindString(parameterIndex++, InstalledAppAdapter.slugAdapter.encode(slug))
      bindBoolean(parameterIndex++, active)
      bindLong(parameterIndex++, version)
      bindString(parameterIndex++, InstalledAppAdapter.wasmo_file_addressAdapter.encode(wasmo_file_address))
    }.also {
      notifyQueries(1_804_962_849) { emit ->
        emit("InstalledApp")
      }
    }

    override fun toString(): String = "InstalledApp.sq:insertInstalledApp"
  }

  private inner class SelectInstalledAppsByComputerIdQuery<out T : Any>(
    public val computer_id: ComputerId,
    public val active: Boolean?,
    public val limit: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Listener) {
      driver.addListener("InstalledApp", "InstalledAppRelease", listener = listener)
    }

    override fun removeListener(listener: Listener) {
      driver.removeListener("InstalledApp", "InstalledAppRelease", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(null, """
    |SELECT
    |  ia.id, ia.installed_at, ia.computer_id, ia.slug, ia.active, ia.version, ia.wasmo_file_address, ia.active_release_id,
    |  iar.id, iar.first_active_at, iar.computer_id, iar.installed_app_id, iar.app_version, iar.app_manifest_data
    |FROM InstalledApp ia
    |LEFT JOIN InstalledAppRelease iar
    |  ON ia.active_release_id = iar.id
    |WHERE
    |  ia.computer_id = ? AND
    |  ia.active ${ if (active == null) "IS" else "=" } ?
    |ORDER BY slug
    |LIMIT ?
    """.trimMargin(), mapper, 3) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, InstalledAppAdapter.computer_idAdapter.encode(computer_id))
      bindBoolean(parameterIndex++, active)
      bindLong(parameterIndex++, limit)
    }

    override fun toString(): String = "InstalledApp.sq:selectInstalledAppsByComputerId"
  }

  private inner class SelectInstalledAppByComputerIdAndSlugQuery<out T : Any>(
    public val computer_id: ComputerId,
    public val slug: AppSlug,
    public val active: Boolean?,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Listener) {
      driver.addListener("InstalledApp", "InstalledAppRelease", listener = listener)
    }

    override fun removeListener(listener: Listener) {
      driver.removeListener("InstalledApp", "InstalledAppRelease", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(null, """
    |SELECT
    |  ia.id, ia.installed_at, ia.computer_id, ia.slug, ia.active, ia.version, ia.wasmo_file_address, ia.active_release_id,
    |  iar.id, iar.first_active_at, iar.computer_id, iar.installed_app_id, iar.app_version, iar.app_manifest_data
    |FROM InstalledApp ia
    |LEFT JOIN InstalledAppRelease iar
    |  ON ia.active_release_id = iar.id
    |WHERE
    |  ia.computer_id = ? AND
    |  ia.slug = ? AND
    |  ia.active ${ if (active == null) "IS" else "=" } ?
    |LIMIT 1
    """.trimMargin(), mapper, 3) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, InstalledAppAdapter.computer_idAdapter.encode(computer_id))
      bindString(parameterIndex++, InstalledAppAdapter.slugAdapter.encode(slug))
      bindBoolean(parameterIndex++, active)
    }

    override fun toString(): String = "InstalledApp.sq:selectInstalledAppByComputerIdAndSlug"
  }

  private inner class SelectInstalledAppByIdQuery<out T : Any>(
    public val id: InstalledAppId,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Listener) {
      driver.addListener("InstalledApp", listener = listener)
    }

    override fun removeListener(listener: Listener) {
      driver.removeListener("InstalledApp", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_170_206_678, """
    |SELECT InstalledApp.id, InstalledApp.installed_at, InstalledApp.computer_id, InstalledApp.slug, InstalledApp.active, InstalledApp.version, InstalledApp.wasmo_file_address, InstalledApp.active_release_id FROM InstalledApp
    |WHERE id = ?
    |LIMIT 1
    """.trimMargin(), mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, InstalledAppAdapter.idAdapter.encode(id))
    }

    override fun toString(): String = "InstalledApp.sq:selectInstalledAppById"
  }
}
