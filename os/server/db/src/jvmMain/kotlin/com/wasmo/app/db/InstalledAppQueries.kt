package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor as JdbcCursor
import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as ExecutableQuery
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import kotlin.time.Instant
import wasmo.sql.RowIterator

public class InstalledAppQueries(
  private val driver: SqlDriver,
  private val InstalledAppAdapter: InstalledApp.Adapter,
  private val InstalledAppReleaseAdapter: InstalledAppRelease.Adapter,
) {
  public fun insertInstalledApp(
    installed_at: Instant,
    computer_id: ComputerId,
    slug: AppSlug,
    active: Boolean?,
    version: Long,
    wasmo_file_address: WasmoFileAddress,
  ): ExecutableQuery<InstalledAppId> = InsertInstalledAppQuery(
    installed_at,
    computer_id,
    slug,
    active,
    version,
    wasmo_file_address,
  ) { cursor ->
    InstalledAppAdapter.idAdapter.decode(cursor.getS64(0)!!)
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
    mapper(
      InstalledAppAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      InstalledAppAdapter.computer_idAdapter.decode(cursor.getS64(2)!!),
      InstalledAppAdapter.slugAdapter.decode(cursor.getString(3)!!),
      cursor.getBool(4),
      cursor.getS64(5)!!,
      InstalledAppAdapter.wasmo_file_addressAdapter.decode(cursor.getString(6)!!),
      cursor.getS64(7)?.let { InstalledAppAdapter.active_release_idAdapter.decode(it) },
      cursor.getS64(8)?.let { InstalledAppReleaseAdapter.idAdapter.decode(it) },
      cursor.getInstant(9),
      cursor.getS64(10)?.let { InstalledAppReleaseAdapter.computer_idAdapter.decode(it) },
      cursor.getS64(11)?.let { InstalledAppReleaseAdapter.installed_app_idAdapter.decode(it) },
      cursor.getS64(12),
      cursor.getString(13)?.let { InstalledAppReleaseAdapter.app_manifest_dataAdapter.decode(it) },
    )
  }

  public fun selectInstalledAppsByComputerId(
    computer_id: ComputerId,
    active: Boolean?,
    limit: Long,
  ): Query<SelectInstalledAppsByComputerId> =
    selectInstalledAppsByComputerId(computer_id, active, limit, ::SelectInstalledAppsByComputerId)

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
    mapper(
      InstalledAppAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      InstalledAppAdapter.computer_idAdapter.decode(cursor.getS64(2)!!),
      InstalledAppAdapter.slugAdapter.decode(cursor.getString(3)!!),
      cursor.getBool(4),
      cursor.getS64(5)!!,
      InstalledAppAdapter.wasmo_file_addressAdapter.decode(cursor.getString(6)!!),
      cursor.getS64(7)?.let { InstalledAppAdapter.active_release_idAdapter.decode(it) },
      cursor.getS64(8)?.let { InstalledAppReleaseAdapter.idAdapter.decode(it) },
      cursor.getInstant(9),
      cursor.getS64(10)?.let { InstalledAppReleaseAdapter.computer_idAdapter.decode(it) },
      cursor.getS64(11)?.let { InstalledAppReleaseAdapter.installed_app_idAdapter.decode(it) },
      cursor.getS64(12),
      cursor.getString(13)?.let { InstalledAppReleaseAdapter.app_manifest_dataAdapter.decode(it) },
    )
  }

  public fun selectInstalledAppByComputerIdAndSlug(
    computer_id: ComputerId,
    slug: AppSlug,
    active: Boolean?,
  ): Query<SelectInstalledAppByComputerIdAndSlug> = selectInstalledAppByComputerIdAndSlug(
    computer_id,
    slug,
    active,
    ::SelectInstalledAppByComputerIdAndSlug,
  )

  public fun <T : Any> selectInstalledAppById(
    id: InstalledAppId,
    mapper: (
      id: InstalledAppId,
      installed_at: Instant,
      computer_id: ComputerId,
      slug: AppSlug,
      active: Boolean?,
      version: Long,
      wasmo_file_address: WasmoFileAddress,
      active_release_id: InstalledAppReleaseId?,
    ) -> T,
  ): Query<T> = SelectInstalledAppByIdQuery(id) { cursor ->
    mapper(
      InstalledAppAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      InstalledAppAdapter.computer_idAdapter.decode(cursor.getS64(2)!!),
      InstalledAppAdapter.slugAdapter.decode(cursor.getString(3)!!),
      cursor.getBool(4),
      cursor.getS64(5)!!,
      InstalledAppAdapter.wasmo_file_addressAdapter.decode(cursor.getString(6)!!),
      cursor.getS64(7)?.let { InstalledAppAdapter.active_release_idAdapter.decode(it) },
    )
  }

  public fun selectInstalledAppById(id: InstalledAppId): Query<InstalledApp> =
    selectInstalledAppById(id, ::InstalledApp)

  /**
   * @return The number of rows updated.
   */
  public suspend fun setRelease(
    new_version: Long,
    active_release_id: InstalledAppReleaseId?,
    expected_version: Long,
    id: InstalledAppId,
  ): Long {
    val result = driver.execute(
      """
          |UPDATE InstalledApp
          |SET
          |  version = $1,
          |  active_release_id = $2
          |WHERE
          |  version = $3 AND
          |  id = $4
          """.trimMargin(),
    ) {
      var parameterIndex = 0
      bindS64(parameterIndex++, new_version)
      bindS64(
        parameterIndex++,
        active_release_id?.let { InstalledAppAdapter.active_release_idAdapter.encode(it) },
      )
      bindS64(parameterIndex++, expected_version)
      bindS64(parameterIndex++, InstalledAppAdapter.idAdapter.encode(id))
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
    mapper: suspend (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override suspend fun execute(): RowIterator {
      return driver.executeQuery(
        """
          |INSERT INTO InstalledApp(
          |  installed_at,
          |  computer_id,
          |  slug,
          |  active,
          |  version,
          |  wasmo_file_address
          |)
          |VALUES (
          |  $1,
          |  $2,
          |  $3,
          |  $4,
          |  $5,
          |  $6
          |) RETURNING id
          """.trimMargin(),
      ) {
        var parameterIndex = 0
        bindInstant(parameterIndex++, installed_at)
        bindS64(parameterIndex++, InstalledAppAdapter.computer_idAdapter.encode(computer_id))
        bindString(parameterIndex++, InstalledAppAdapter.slugAdapter.encode(slug))
        bindBool(parameterIndex++, active)
        bindS64(parameterIndex++, version)
        bindString(
          parameterIndex++,
          InstalledAppAdapter.wasmo_file_addressAdapter.encode(wasmo_file_address),
        )
      }
    }

    override fun toString(): String = "InstalledApp.sq:insertInstalledApp"
  }

  private inner class SelectInstalledAppsByComputerIdQuery<out T : Any>(
    public val computer_id: ComputerId,
    public val active: Boolean?,
    public val limit: Long,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun execute(): RowIterator {
      return driver.executeQuery(
        """
          |SELECT
          |  ia.id, ia.installed_at, ia.computer_id, ia.slug, ia.active, ia.version, ia.wasmo_file_address, ia.active_release_id,
          |  iar.id, iar.first_active_at, iar.computer_id, iar.installed_app_id, iar.app_version, iar.app_manifest_data
          |FROM InstalledApp ia
          |LEFT JOIN InstalledAppRelease iar
          |  ON ia.active_release_id = iar.id
          |WHERE
          |  ia.computer_id = $1 AND
          |  ia.active ${if (active == null) "IS" else "="} $2
          |ORDER BY slug
          |LIMIT $3
          """.trimMargin(),
      ) {
        var parameterIndex = 0
        bindS64(parameterIndex++, InstalledAppAdapter.computer_idAdapter.encode(computer_id))
        bindBool(parameterIndex++, active)
        bindS64(parameterIndex++, limit)
      }
    }

    override fun toString(): String = "InstalledApp.sq:selectInstalledAppsByComputerId"
  }

  private inner class SelectInstalledAppByComputerIdAndSlugQuery<out T : Any>(
    public val computer_id: ComputerId,
    public val slug: AppSlug,
    public val active: Boolean?,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun execute(): RowIterator {
      return driver.executeQuery(
        """
          |SELECT
          |  ia.id, ia.installed_at, ia.computer_id, ia.slug, ia.active, ia.version, ia.wasmo_file_address, ia.active_release_id,
          |  iar.id, iar.first_active_at, iar.computer_id, iar.installed_app_id, iar.app_version, iar.app_manifest_data
          |FROM InstalledApp ia
          |LEFT JOIN InstalledAppRelease iar
          |  ON ia.active_release_id = iar.id
          |WHERE
          |  ia.computer_id = $1 AND
          |  ia.slug = $2 AND
          |  ia.active ${if (active == null) "IS" else "="} $3
          |LIMIT 1
          """.trimMargin()
      ) {
        var parameterIndex = 0
        bindS64(parameterIndex++, InstalledAppAdapter.computer_idAdapter.encode(computer_id))
        bindString(parameterIndex++, InstalledAppAdapter.slugAdapter.encode(slug))
        bindBool(parameterIndex++, active)
      }
    }

    override fun toString(): String = "InstalledApp.sq:selectInstalledAppByComputerIdAndSlug"
  }

  private inner class SelectInstalledAppByIdQuery<out T : Any>(
    public val id: InstalledAppId,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun execute(): RowIterator {
      return driver.executeQuery(
        """
          |SELECT InstalledApp.id, InstalledApp.installed_at, InstalledApp.computer_id, InstalledApp.slug, InstalledApp.active, InstalledApp.version, InstalledApp.wasmo_file_address, InstalledApp.active_release_id FROM InstalledApp
          |WHERE id = $1
          |LIMIT 1
          """.trimMargin()
      ) {
        var parameterIndex = 0
        bindS64(parameterIndex++, InstalledAppAdapter.idAdapter.encode(id))
      }
    }

    override fun toString(): String = "InstalledApp.sq:selectInstalledAppById"
  }
}
