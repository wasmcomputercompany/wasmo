package com.wasmo.installedapps

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.db.InstalledApp
import com.wasmo.db.InstalledAppRelease
import com.wasmo.db.SelectInstalledAppByComputerIdAndSlug
import com.wasmo.db.WasmoDb
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.InstalledAppId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class RealInstalledAppStore(
  private val wasmoDb: WasmoDb,
  private val installedAppServiceGraphFactory: InstalledAppServiceGraph.Factory,
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
    ).executeAsOneOrNull()
      ?: return null

    return get(
      computerSlug = computer.slug,
      installedApp = row.installedApp,
      installedAppRelease = row.installedAppRelease ?: return null,
    )
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun get(installedAppId: InstalledAppId): InstalledAppService? {
    val installedApp = wasmoDb.installedAppQueries.selectInstalledAppById(installedAppId)
      .executeAsOne()
    val installedAppRelease = wasmoDb.installedAppReleaseQueries
      .selectInstalledAppReleaseById(installedApp.active_release_id ?: return null)
      .executeAsOne()
    return get(
      installedApp = installedApp,
      installedAppRelease = installedAppRelease,
    )
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun get(
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease,
  ): InstalledAppService {
    val computer = wasmoDb.computerQueries.selectComputerById(installedApp.computer_id)
      .executeAsOne()
    return get(computer.slug, installedApp, installedAppRelease)
  }

  override fun get(
    computerSlug: ComputerSlug,
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease,
  ): InstalledAppService {
    val graph = installedAppServiceGraphFactory.create(
      computerSlug = computerSlug,
      installedApp = installedApp,
      installedAppRelease = installedAppRelease,
    )
    return graph.service
  }

  private val SelectInstalledAppByComputerIdAndSlug.installedApp: InstalledApp
    get() = InstalledApp(
      id = id,
      installed_at = installed_at,
      computer_id = computer_id,
      slug = slug,
      active = active,
      version = version,
      wasmo_file_address = wasmo_file_address,
      active_release_id = active_release_id,
    )

  private val SelectInstalledAppByComputerIdAndSlug.installedAppRelease: InstalledAppRelease?
    get() {
      return InstalledAppRelease(
        id = id_ ?: return null,
        first_active_at = first_active_at ?: return null,
        computer_id = computer_id_ ?: return null,
        installed_app_id = installed_app_id ?: return null,
        app_version = app_version ?: return null,
        app_manifest_data = app_manifest_data ?: return null,
      )
    }
}
