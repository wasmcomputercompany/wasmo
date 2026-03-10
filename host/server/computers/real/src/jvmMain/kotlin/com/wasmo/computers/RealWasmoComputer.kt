package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InstallIncompleteReason
import com.wasmo.api.InstalledApp
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import okhttp3.HttpUrl

@Inject
@SingleIn(ComputerScope::class)
class RealWasmoComputer(
  override val id: ComputerId,
  override val slug: ComputerSlug,
  override val appInstaller: AppInstaller,
  override val manifestLoader: ManifestLoader,
  private val clock: Clock,
  private val deployment: Deployment,
  private val wasmoDb: WasmoDb,
  private val appCatalog: AppCatalog,
) : WasmoComputer {
  override val url: HttpUrl
    get() = deployment.baseUrl.resolve("/computer/${slug.value}")!!

  context(transactionCallbacks: TransactionCallbacks)
  override fun initialize() {
    for (entry in appCatalog.entries) {
      wasmoDb.appInstallQueries.insertAppInstall(
        computer_id = id,
        slug = entry.slug,
        manifest_url = "https://apps.wasmo.com/${slug}/manifest.json",
        launcher_label = entry.launcherLabel,
        version = 1L,
        install_scheduled_at = clock.now(),
      ).executeAsOne()
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
        InstalledApp(
          slug = appInstall.slug,
          launcherLabel = appInstall.launcher_label ?: appInstall.slug.value,
          maskableIconUrl = "/assets/launcher/sample-folder.svg",
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
