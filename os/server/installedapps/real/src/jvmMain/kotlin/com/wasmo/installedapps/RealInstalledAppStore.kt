package com.wasmo.installedapps

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.app.db.InstalledAppAndRelease
import com.wasmo.db.InstalledApp
import com.wasmo.db.InstalledAppRelease
import com.wasmo.db.WasmoDb
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class RealInstalledAppStore(
  private val wasmoDb: WasmoDb,
  private val installedAppServiceGraphFactory: InstalledAppServiceGraph.Factory,
  private val appManifestLoaderFactory: RealAppManifestLoaderFactory,
) : InstalledAppStore {

  context(transactionCallbacks: TransactionCallbacks)
  override fun getOrNull(
    client: Client,
    computerSlug: ComputerSlug,
    appSlug: AppSlug,
  ): InstalledAppService? {
    val accountId = client.getAccountIdOrNull()
      ?: return null

    val computer = wasmoDb.computerQueries.selectComputerByAccountIdAndSlug(
      account_id = accountId,
      slug = computerSlug,
    ).executeAsOneOrNull()
      ?: return null

    val row = wasmoDb.installedAppQueries.selectInstalledAppByComputerIdAndSlug(
      computer_id = computer.id,
      slug = appSlug,
      active = true,
      mapper = InstalledAppAndRelease::invoke,
    ).executeAsOneOrNull()
      ?: return null

    return get(
      computerSlug = computer.slug,
      installedApp = row.installedApp,
      installedAppRelease = row.installedAppRelease,
    )
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun get(installedAppId: InstalledAppId): InstalledAppService? {
    val installedApp = wasmoDb.installedAppQueries.selectInstalledAppById(installedAppId)
      .executeAsOne()
    val installedAppRelease = wasmoDb.installedAppReleaseQueries
      .selectInstalledAppReleaseById(installedApp.active_release_id ?: return null)
      .executeAsOneOrNull()
    return get(
      installedApp = installedApp,
      installedAppRelease = installedAppRelease,
    )
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun get(
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease?,
  ): InstalledAppService {
    val computer = wasmoDb.computerQueries.selectComputerById(installedApp.computer_id)
      .executeAsOne()
    return get(computer.slug, installedApp, installedAppRelease)
  }

  override fun get(
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
