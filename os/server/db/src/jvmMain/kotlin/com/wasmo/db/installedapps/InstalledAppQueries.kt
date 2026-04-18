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
import com.wasmo.sql.list
import com.wasmo.sql.single
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow

context(connection: SqlConnection)
suspend fun insertInstalledApp(
  installed_at: Instant,
  computer_id: ComputerId,
  slug: AppSlug,
  active: Boolean?,
  version: Long,
  wasmo_file_address: WasmoFileAddress,
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
    bindInstant(0, installed_at)
    bindComputerId(1, computer_id)
    bindAppSlug(2, slug)
    bindBool(3, active)
    bindS64(4, version)
    bindWasmoFileAddress(5, wasmo_file_address)
  }
  return rowIterator.single {
    getInstalledAppId(0)
  }
}

context(connection: SqlConnection)
suspend fun selectInstalledAppsByComputerId(
  computer_id: ComputerId,
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
    bindComputerId(0, computer_id)
    bindBool(1, active)
    bindS64(2, limit)
  }

  return rowIterator.list {
    getInstalledAppAndRelease()
  }
}

private fun SqlRow.getInstalledAppAndRelease(): InstalledAppAndRelease {
  val installedApp = InstalledApp(
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
    else -> InstalledAppRelease(
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
  computer_id: ComputerId,
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
    bindComputerId(0, computer_id)
    bindAppSlug(1, slug)
    bindBool(2, active)
  }
  return rowIterator.singleOrNull {
    getInstalledAppAndRelease()
  }
}

context(connection: SqlConnection)
suspend fun selectInstalledAppById(id: InstalledAppId): InstalledApp {
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
  new_version: Long,
  active_release_id: InstalledAppReleaseId?,
  expected_version: Long,
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
    bindS64(0, new_version)
    bindInstalledAppReleaseId(1, active_release_id)
    bindS64(2, expected_version)
    bindInstalledAppId(3, id)
  }
}

private fun SqlRow.getInstalledApp() = InstalledApp(
  id = getInstalledAppId(0),
  installed_at = getInstant(1)!!,
  computer_id = getComputerId(2),
  slug = getAppSlug(3),
  active = getBool(4),
  version = getS64(5)!!,
  wasmo_file_address = getWasmoFileAddress(6),
  active_release_id = getInstalledAppReleaseIdOrNull(7),
)

