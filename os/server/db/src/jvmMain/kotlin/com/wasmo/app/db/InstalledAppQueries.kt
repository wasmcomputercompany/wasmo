package com.wasmo.app.db

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.sql.decodeJson
import com.wasmo.sql.list
import com.wasmo.sql.single
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection

suspend fun SqlConnection.insertInstalledApp(
  installed_at: Instant,
  computer_id: ComputerId,
  slug: AppSlug,
  active: Boolean?,
  version: Long,
  wasmo_file_address: WasmoFileAddress,
): InstalledAppId {
  val rowIterator = executeQuery(
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
    var parameterIndex = 0
    bindInstant(parameterIndex++, installed_at)
    bindComputerId(parameterIndex++, computer_id)
    bindAppSlug(parameterIndex++, slug)
    bindBool(parameterIndex++, active)
    bindS64(parameterIndex++, version)
    bindWasmoFileAddress(parameterIndex++, wasmo_file_address)
  }
  return rowIterator.single { cursor ->
    cursor.getInstalledAppId(0)
  }
}

suspend fun SqlConnection.selectInstalledAppsByComputerId(
  computer_id: ComputerId,
  active: Boolean?,
  limit: Long,
): List<InstalledAppAndRelease> {
  val rowIterator = executeQuery(
    """
    SELECT
      ia.id, ia.installed_at, ia.computer_id, ia.slug, ia.active, ia.version, ia.wasmo_file_address, ia.active_release_id,
      iar.id, iar.first_active_at, iar.computer_id, iar.installed_app_id, iar.app_version, iar.app_manifest_data
    FROM InstalledApp ia
    LEFT JOIN InstalledAppRelease iar
      ON ia.active_release_id = iar.id
    WHERE
      ia.computer_id = $1 AND
      ia.active ${if (active == null) "IS" else "="} $2
    ORDER BY slug
    LIMIT $3
    """,
  ) {
    var parameterIndex = 0
    bindComputerId(parameterIndex++, computer_id)
    bindBool(parameterIndex++, active)
    bindS64(parameterIndex++, limit)
  }

  return rowIterator.list { cursor ->
    InstalledAppAndRelease(
      cursor.getInstalledAppId(0),
      cursor.getInstant(1)!!,
      cursor.getComputerId(2),
      cursor.getAppSlug(3),
      cursor.getBool(4),
      cursor.getS64(5)!!,
      cursor.getWasmoFileAddress(6),
      cursor.getInstalledAppReleaseIdOrNull(7),
      cursor.getInstalledAppReleaseIdOrNull(8),
      cursor.getInstant(9),
      cursor.getComputerIdOrNull(10),
      cursor.getInstalledAppId(11),
      cursor.getS64(12),
      cursor.decodeJson<AppManifest>(13),
    )
  }
}

suspend fun SqlConnection.selectInstalledAppByComputerIdAndSlug(
  computer_id: ComputerId,
  slug: AppSlug,
  active: Boolean?,
): InstalledAppAndRelease? {
  val rowIterator = executeQuery(
    """
    SELECT
      ia.id, ia.installed_at, ia.computer_id, ia.slug, ia.active, ia.version, ia.wasmo_file_address, ia.active_release_id,
      iar.id, iar.first_active_at, iar.computer_id, iar.installed_app_id, iar.app_version, iar.app_manifest_data
    FROM InstalledApp ia
    LEFT JOIN InstalledAppRelease iar
      ON ia.active_release_id = iar.id
    WHERE
      ia.computer_id = $1 AND
      ia.slug = $2 AND
      ia.active ${if (active == null) "IS" else "="} $3
    LIMIT 1
    """,
  ) {
    var parameterIndex = 0
    bindComputerId(parameterIndex++, computer_id)
    bindAppSlug(parameterIndex++, slug)
    bindBool(parameterIndex++, active)
  }
  return rowIterator.singleOrNull { cursor ->
    InstalledAppAndRelease(
      cursor.getInstalledAppId(0),
      cursor.getInstant(1)!!,
      cursor.getComputerId(2),
      cursor.getAppSlug(3),
      cursor.getBool(4),
      cursor.getS64(5)!!,
      cursor.getWasmoFileAddress(6),
      cursor.getInstalledAppReleaseIdOrNull(7),
      cursor.getInstalledAppReleaseIdOrNull(8),
      cursor.getInstant(9),
      cursor.getComputerIdOrNull(10),
      cursor.getInstalledAppIdOrNull(11),
      cursor.getS64(12),
      cursor.decodeJson<AppManifest>(13),
    )
  }
}

suspend fun SqlConnection.selectInstalledAppById(id: InstalledAppId): InstalledApp {
  val rowIterator = executeQuery(
    """
    SELECT InstalledApp.id, InstalledApp.installed_at, InstalledApp.computer_id, InstalledApp.slug, InstalledApp.active, InstalledApp.version, InstalledApp.wasmo_file_address, InstalledApp.active_release_id FROM InstalledApp
    WHERE id = $1
    LIMIT 1
    """,
  ) {
    var parameterIndex = 0
    bindInstalledAppId(parameterIndex++, id)
  }

  return rowIterator.single { cursor ->
    InstalledApp(
      cursor.getInstalledAppId(0),
      cursor.getInstant(1)!!,
      cursor.getComputerId(2),
      cursor.getAppSlug(3),
      cursor.getBool(4),
      cursor.getS64(5)!!,
      cursor.getWasmoFileAddress(6),
      cursor.getInstalledAppReleaseIdOrNull(7),
    )
  }
}

suspend fun SqlConnection.setRelease(
  new_version: Long,
  active_release_id: InstalledAppReleaseId?,
  expected_version: Long,
  id: InstalledAppId,
): Long {
  return execute(
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
    var parameterIndex = 0
    bindS64(parameterIndex++, new_version)
    bindInstalledAppReleaseId(parameterIndex++, active_release_id)
    bindS64(parameterIndex++, expected_version)
    bindInstalledAppId(parameterIndex++, id)
  }
}
