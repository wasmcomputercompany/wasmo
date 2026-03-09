package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InstalledApp
import com.wasmo.db.AppInstall
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.jobs.JobQueue
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlin.time.Instant
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

@Inject
@SingleIn(ComputerScope::class)
class RealWasmoComputer(
  override val id: ComputerId,
  override val slug: ComputerSlug,
  private val clock: Clock,
  private val deployment: Deployment,
  private val wasmoDb: WasmoDb,
  private val appInstaller: AppInstaller,
  private val installAppJobQueue: JobQueue<InstallAppJob>,
  private val manifestLoader: ManifestLoader,
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

  override suspend fun enqueueInstallApp(manifestUrl: HttpUrl) {
    val manifest = manifestLoader.loadManifest(
      manifestUrl = manifestUrl,
    )

    wasmoDb.transaction(noEnclosing = true) {
      val appInstallId = wasmoDb.appInstallQueries.insertAppInstall(
        computer_id = id,
        slug = AppSlug(manifest.slug),
        manifest_url = manifestUrl.toString(),
        launcher_label = manifest.launcher?.label,
        version = manifest.version,
        install_scheduled_at = clock.now(),
      ).executeAsOne()

      installAppJobQueue.enqueue(InstallAppJob(appInstallId))
    }
  }

  override suspend fun enqueueInstallApp(appInstall: AppInstall) {
    val manifestUrl = appInstall.manifest_url.toHttpUrl()
    val manifest = manifestLoader.loadManifest(manifestUrl)
    appInstaller.install(manifestUrl, manifest)
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun snapshot(): ComputerSnapshot {
    val appInstalls = wasmoDb.appInstallQueries.selectAppInstallsByComputerId(
      computer_id = id,
      limit = 100,
    ).executeAsList()

    val installScheduledAt = Instant.fromEpochSeconds(0L)

    return ComputerSnapshot(
      slug = slug,
      apps = appInstalls.map {
        InstalledApp(
          slug = it.slug,
          launcherLabel = it.launcher_label ?: it.slug.value,
          maskableIconUrl = "/assets/launcher/sample-folder.svg",
          installScheduledAt = installScheduledAt,
        )
      },
    )
  }
}
