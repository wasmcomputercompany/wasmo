package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InstallIncompleteReason
import com.wasmo.api.InstalledApp
import com.wasmo.db.WasmoDb
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ComputerScope::class)
class RealWasmoComputer(
  override val id: ComputerId,
  override val slug: ComputerSlug,
  override val appInstaller: AppInstaller,
  override val manifestLoader: ManifestLoader,
  private val wasmoDb: WasmoDb,
  private val appCatalog: AppCatalog,
  private val urlFactory: ComputerUrlFactory,
) : WasmoComputer {
  context(transactionCallbacks: TransactionCallbacks)
  override fun initialize() {
    for (entry in appCatalog.entries) {
      appInstaller.enqueueInstall(
        manifestUrl = entry.manifestUrl,
        manifest = entry.manifest,
      )
    }
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun snapshot(): ComputerSnapshot {
    val appInstalls = wasmoDb.appInstallQueries.selectAppInstallsByComputerId(
      computer_id = id,
      limit = 100,
    ).executeAsList()

    return ComputerSnapshot(
      slug = slug,
      apps = appInstalls.map { appInstall ->
        val appUrl = urlFactory.appUrl(appInstall.slug)
        val maskableIconUrl = appInstall.manifest_data.launcher?.maskable_icon_path
          ?.let { appUrl.resolve(it) }
          ?: appUrl.resolve("/maskable-icon.svg")

        InstalledApp(
          slug = appInstall.slug,
          launcherLabel = appInstall.manifest_data.launcher?.label ?: appInstall.slug.value,
          maskableIconUrl = maskableIconUrl.toString(),
          installScheduledAt = appInstall.install_scheduled_at,
          installCompletedAt = appInstall.install_completed_at,
          installDeletedAt = appInstall.install_deleted_at,
          installIncompleteReason = appInstall.install_incomplete_reason
            ?.let { InstallIncompleteReason.valueOf(it) },
        )
      },
    )
  }
}
