package com.wasmo.app.db

import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.app.db2.bindComputerId
import com.wasmo.app.db2.bindInstalledAppId
import com.wasmo.app.db2.bindInstalledAppReleaseId
import com.wasmo.app.db2.bindJson
import com.wasmo.app.db2.getComputerId
import com.wasmo.app.db2.getInstalledAppId
import com.wasmo.app.db2.getInstalledAppReleaseId
import com.wasmo.app.db2.getJson2
import com.wasmo.app.db2.single
import com.wasmo.app.db2.singleOrNull
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.packaging.AppManifest
import kotlin.time.Instant

class InstalledAppReleaseQueries(
  private val driver: SqlDriver,
) {
  suspend fun insertInstalledAppRelease(
    first_active_at: Instant,
    computer_id: ComputerId,
    installed_app_id: InstalledAppId,
    app_version: Long,
    app_manifest_data: AppManifest,
  ): InstalledAppReleaseId {
    val rowIterator = driver.executeQuery(
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
      var parameterIndex = 0
      bindInstant(parameterIndex++, first_active_at)
      bindComputerId(parameterIndex++, computer_id)
      bindInstalledAppId(parameterIndex++, installed_app_id)
      bindS64(parameterIndex++, app_version)
      bindJson(parameterIndex++, app_manifest_data)
    }
    return rowIterator.single { cursor ->
      cursor.getInstalledAppReleaseId(0)
    }
  }

  suspend fun selectInstalledAppReleaseById(
    id: InstalledAppReleaseId,
  ): InstalledAppRelease? {
    val rowIterator = driver.executeQuery(
      """
      SELECT InstalledAppRelease.id, InstalledAppRelease.first_active_at, InstalledAppRelease.computer_id, InstalledAppRelease.installed_app_id, InstalledAppRelease.app_version, InstalledAppRelease.app_manifest_data FROM InstalledAppRelease
      WHERE id = $1
      LIMIT 1
      """
    ) {
      var parameterIndex = 0
      bindInstalledAppReleaseId(parameterIndex++, id)
    }

    return rowIterator.singleOrNull { cursor ->
      InstalledAppRelease(
        cursor.getInstalledAppReleaseId(0),
        cursor.getInstant(1)!!,
        cursor.getComputerId(2),
        cursor.getInstalledAppId(3),
        cursor.getS64(4)!!,
        cursor.getJson2<AppManifest>(5),
      )
    }
  }
}
