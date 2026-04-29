package com.wasmo.db.installedapps

import com.wasmo.db.bindAppSlug
import com.wasmo.db.bindComputerId
import com.wasmo.db.bindInstalledAppId
import com.wasmo.db.bindInstalledAppReleaseId
import com.wasmo.db.bindWasmoFileAddress
import com.wasmo.db.decodeJson
import com.wasmo.db.getAppSlug
import com.wasmo.db.getComputerId
import com.wasmo.db.getComputerIdOrNull
import com.wasmo.db.getInstalledAppId
import com.wasmo.db.getInstalledAppReleaseIdOrNull
import com.wasmo.db.getWasmoFileAddress
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import kotlin.time.Instant
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow
import wasmox.sql.list
import wasmox.sql.single
import wasmox.sql.singleOrNull

context(connection: SqlConnection)
suspend fun insertInstalledApp(
  installedAt: Instant,
  computerId: ComputerId,
  slug: AppSlug,
  active: Boolean?,
  version: Long,
  wasmoFileAddress: WasmoFileAddress,
): InstalledAppId {
  val rowIterator = connection.executeQuery(
    """
    INSERT INTO InstalledApp(
      installed_at,
      computer_id,
      slug,
      active,
      version,
      wasmo_file_address
    )
    VALUES (
      $1,
      $2,
      $3,
      $4,
      $5,
      $6
    ) RETURNING id
    """,
  ) {
    bindInstant(0, installedAt)
    bindComputerId(1, computerId)
    bindAppSlug(2, slug)
    bindBool(3, active)
    bindS64(4, version)
    bindWasmoFileAddress(5, wasmoFileAddress)
  }
  return rowIterator.single {
    getInstalledAppId(0)
  }
}

context(connection: SqlConnection)
suspend fun selectInstalledAppsByComputerId(
  computerId: ComputerId,
  active: Boolean?,
  limit: Long,
): List<InstalledAppAndRelease> {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      ia.id,
      ia.installed_at,
      ia.computer_id,
      ia.slug,
      ia.active,
      ia.version,
      ia.wasmo_file_address,
      ia.active_release_id,
      iar.id,
      iar.first_active_at,
      iar.computer_id,
      iar.installed_app_id,
      iar.app_version,
      iar.app_manifest_data
    FROM InstalledApp ia
    LEFT JOIN InstalledAppRelease iar ON ia.active_release_id = iar.id
    WHERE ia.computer_id = $1 AND
      ia.active ${if (active == null) "IS" else "="} $2
    ORDER BY slug
    LIMIT $3
    """,
  ) {
    bindComputerId(0, computerId)
    bindBool(1, active)
    bindS64(2, limit)
  }

  return rowIterator.list {
    getInstalledAppAndRelease()
  }
}

private fun SqlRow.getInstalledAppAndRelease(): InstalledAppAndRelease {
  val installedApp = DbInstalledApp(
    getInstalledAppId(0),
    getInstant(1)!!,
    getComputerId(2),
    getAppSlug(3),
    getBool(4),
    getS64(5)!!,
    getWasmoFileAddress(6),
    getInstalledAppReleaseIdOrNull(7),
  )

  val release = when (val releaseId = getInstalledAppReleaseIdOrNull(8)) {
    null -> null
    else -> DbInstalledAppRelease(
      releaseId,
      getInstant(9)!!,
      getComputerIdOrNull(10)!!,
      getInstalledAppId(11),
      getS64(12)!!,
      decodeJson<AppManifest>(13),
    )
  }

  return InstalledAppAndRelease(
    installedApp,
    release,
  )
}

context(connection: SqlConnection)
suspend fun selectInstalledAppByComputerIdAndSlug(
  computerId: ComputerId,
  slug: AppSlug,
  active: Boolean?,
): InstalledAppAndRelease? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      ia.id,
      ia.installed_at,
      ia.computer_id,
      ia.slug,
      ia.active,
      ia.version,
      ia.wasmo_file_address,
      ia.active_release_id,
      iar.id,
      iar.first_active_at,
      iar.computer_id,
      iar.installed_app_id,
      iar.app_version,
      iar.app_manifest_data
    FROM InstalledApp ia
    LEFT JOIN InstalledAppRelease iar ON ia.active_release_id = iar.id
    WHERE ia.computer_id = $1 AND
      ia.slug = $2 AND
      ia.active ${if (active == null) "IS" else "="} $3
    LIMIT 1
    """,
  ) {
    bindComputerId(0, computerId)
    bindAppSlug(1, slug)
    bindBool(2, active)
  }
  return rowIterator.singleOrNull {
    getInstalledAppAndRelease()
  }
}

context(connection: SqlConnection)
suspend fun selectInstalledAppById(id: InstalledAppId): DbInstalledApp {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      installed_at,
      computer_id,
      slug,
      active,
      version,
      wasmo_file_address,
      active_release_id
    FROM InstalledApp
    WHERE id = $1
    LIMIT 1
    """,
  ) {
    bindInstalledAppId(0, id)
  }

  return rowIterator.single {
    getInstalledApp()
  }
}

context(connection: SqlConnection)
suspend fun setRelease(
  newVersion: Long,
  activeReleaseId: InstalledAppReleaseId?,
  expectedVersion: Long,
  id: InstalledAppId,
): Long {
  return connection.execute(
    """
    UPDATE InstalledApp
    SET
      version = $1,
      active_release_id = $2
    WHERE
      version = $3 AND
      id = $4
    """,
  ) {
    bindS64(0, newVersion)
    bindInstalledAppReleaseId(1, activeReleaseId)
    bindS64(2, expectedVersion)
    bindInstalledAppId(3, id)
  }
}

private fun SqlRow.getInstalledApp() = DbInstalledApp(
  id = getInstalledAppId(0),
  installedAt = getInstant(1)!!,
  computerId = getComputerId(2),
  slug = getAppSlug(3),
  active = getBool(4),
  version = getS64(5)!!,
  wasmoFileAddress = getWasmoFileAddress(6),
  activeReleaseId = getInstalledAppReleaseIdOrNull(7),
)

