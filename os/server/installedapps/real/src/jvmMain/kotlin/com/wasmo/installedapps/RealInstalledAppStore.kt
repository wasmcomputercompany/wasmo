package com.wasmo.installedapps

import com.wasmo.accounts.Client
import com.wasmo.app.db.InstalledApp
import com.wasmo.app.db.InstalledAppRelease
import com.wasmo.sql.SqlTransaction
import com.wasmo.app.db.selectComputerByAccountIdAndSlug
import com.wasmo.app.db.selectComputerById
import com.wasmo.app.db.selectInstalledAppByComputerIdAndSlug
import com.wasmo.app.db.selectInstalledAppById
import com.wasmo.app.db.selectInstalledAppReleaseById
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class RealInstalledAppStore(
  private val installedAppServiceGraphFactory: InstalledAppServiceGraph.Factory,
  private val appManifestLoaderFactory: RealAppManifestLoaderFactory,
) : InstalledAppStore {

  context(sqlTransaction: SqlTransaction)
  override suspend fun getOrNull(
    client: Client,
    computerSlug: ComputerSlug,
    appSlug: AppSlug,
  ): InstalledAppService? {
    val accountId = client.getAccountIdOrNull()
      ?: return null

    val computer = selectComputerByAccountIdAndSlug(
      account_id = accountId,
      slug = computerSlug,
    ) ?: return null

    val row = selectInstalledAppByComputerIdAndSlug(
      computer_id = computer.id,
      slug = appSlug,
      active = true,
    ) ?: return null

    return get(
      computerSlug = computer.slug,
      installedApp = row.installedApp,
      installedAppRelease = row.installedAppRelease,
    )
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun get(installedAppId: InstalledAppId): InstalledAppService? {
    val installedApp = selectInstalledAppById(installedAppId)
    val installedAppRelease = sqlTransaction.selectInstalledAppReleaseById(
      id = installedApp.active_release_id ?: return null,
    )
    return get(
      installedApp = installedApp,
      installedAppRelease = installedAppRelease,
    )
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun get(
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease?,
  ): InstalledAppService {
    val computer = selectComputerById(installedApp.computer_id)
    return get(computer.slug, installedApp, installedAppRelease)
  }

  override suspend fun get(
    computerSlug: ComputerSlug,
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease?,
  ): InstalledAppService {
    val appManifestLoader = appManifestLoaderFactory.create(
      installedApp = installedApp,
      installedAppRelease = installedAppRelease,
    )
    val graph = installedAppServiceGraphFactory.create(
      computerSlug = computerSlug,
      installedApp = installedApp,
      appManifestLoader = appManifestLoader,
    )
    return graph.service
  }
}
