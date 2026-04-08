package com.wasmo.app.db

import com.wasmo.db.InstalledApp
import com.wasmo.db.InstalledAppRelease
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import kotlin.time.Instant

/** Join the [InstalledApp] and [InstalledAppRelease] tables. */
data class InstalledAppAndRelease(
  val installedApp: InstalledApp,
  val installedAppRelease: InstalledAppRelease?,
) {
  companion object {
    operator fun invoke(
      id: InstalledAppId,
      installedAt: Instant,
      computerId: ComputerId,
      slug: AppSlug,
      active: Boolean?,
      version: Long,
      wasmoFileAddress: WasmoFileAddress,
      activeReleaseId: InstalledAppReleaseId?,
      installedAppReleaseId: InstalledAppReleaseId?,
      firstActiveAt: Instant?,
      releaseComputerId: ComputerId?,
      installedAppId: InstalledAppId?,
      appVersion: Long?,
      appManifestData: AppManifest?,
    ): InstalledAppAndRelease {
      val installedApp = InstalledApp(
        id = id,
        installed_at = installedAt,
        computer_id = computerId,
        slug = slug,
        active = active,
        version = version,
        wasmo_file_address = wasmoFileAddress,
        active_release_id = activeReleaseId,
      )
      val installedAppRelease = InstalledAppRelease(
        id = installedAppReleaseId ?: return InstalledAppAndRelease(installedApp, null),
        first_active_at = firstActiveAt ?: return InstalledAppAndRelease(installedApp, null),
        computer_id = releaseComputerId ?: return InstalledAppAndRelease(installedApp, null),
        installed_app_id = installedAppId ?: return InstalledAppAndRelease(installedApp, null),
        app_version = appVersion ?: return InstalledAppAndRelease(installedApp, null),
        app_manifest_data = appManifestData ?: return InstalledAppAndRelease(installedApp, null),
      )
      return InstalledAppAndRelease(
        installedApp = installedApp,
        installedAppRelease = installedAppRelease,
      )
    }
  }
}
