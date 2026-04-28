package com.wasmo.db.installedapps

import com.wasmo.db.bindComputerId
import com.wasmo.db.bindInstalledAppId
import com.wasmo.db.bindInstalledAppReleaseId
import com.wasmo.db.bindJson
import com.wasmo.db.decodeJson
import com.wasmo.db.getComputerId
import com.wasmo.db.getInstalledAppId
import com.wasmo.db.getInstalledAppReleaseId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.packaging.AppManifest
import com.wasmo.sql.single
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection

context(connection: SqlConnection)
suspend fun insertInstalledAppRelease(
  firstActiveAt: Instant,
  computerId: ComputerId,
  installedAppId: InstalledAppId,
  appVersion: Long,
  appManifestData: AppManifest,
): InstalledAppReleaseId {
  val rowIterator = connection.executeQuery(
    """
    INSERT INTO InstalledAppRelease(
      first_active_at,
      computer_id,
      installed_app_id,
      app_version,
      app_manifest_data
    )
    VALUES (
      $1,
      $2,
      $3,
      $4,
      $5
    ) RETURNING id
    """,
  ) {
    bindInstant(0, firstActiveAt)
    bindComputerId(1, computerId)
    bindInstalledAppId(2, installedAppId)
    bindS64(3, appVersion)
    bindJson(4, appManifestData)
  }
  return rowIterator.single {
    getInstalledAppReleaseId(0)
  }
}

context(connection: SqlConnection)
suspend fun selectInstalledAppReleaseById(
  id: InstalledAppReleaseId,
): DbInstalledAppRelease? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      first_active_at,
      computer_id,
      installed_app_id,
      app_version,
      app_manifest_data
    FROM InstalledAppRelease
    WHERE id = $1
    LIMIT 1
    """
  ) {
    bindInstalledAppReleaseId(0, id)
  }

  return rowIterator.singleOrNull {
    DbInstalledAppRelease(
      getInstalledAppReleaseId(0),
      getInstant(1)!!,
      getComputerId(2),
      getInstalledAppId(3),
      getS64(4)!!,
      decodeJson<AppManifest>(5),
    )
  }
}
