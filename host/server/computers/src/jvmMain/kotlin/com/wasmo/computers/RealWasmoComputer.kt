package com.wasmo.computers

import com.wasmo.api.ComputerSlug
import com.wasmo.db.AppInstall
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.ComputerId
import com.wasmo.jobs.JobQueue
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

@Inject
@SingleIn(ComputerScope::class)
class RealWasmoComputer(
  private val clock: Clock,
  private val deployment: Deployment,
  private val wasmoDb: WasmoDb,
  private val computerId: ComputerId,
  private val slug: ComputerSlug,
  override val appLoader: AppLoader,
  private val installAppJobQueue: JobQueue<InstallAppJob>,
  private val manifestLoader: ManifestLoader,
) : WasmoComputer {
  override val url: HttpUrl
    get() = deployment.baseUrl.resolve("/computer/${slug.value}")!!

  override suspend fun enqueueInstallApp(manifestUrl: HttpUrl) {
    val manifest = manifestLoader.loadManifest(
      manifestUrl = manifestUrl,
    )

    wasmoDb.transaction(noEnclosing = true) {
      val appInstallId = wasmoDb.appInstallQueries.insertAppInstall(
        computer_id = computerId,
        slug = manifest.slug,
        manifest_url = manifestUrl.toString(),
        display_name = manifest.displayName,
        version = manifest.version,
        install_scheduled_at = clock.now(),
      ).executeAsOne()

      installAppJobQueue.enqueue(InstallAppJob(appInstallId))
    }
  }

  override suspend fun enqueueInstallApp(appInstall: AppInstall) {
    val manifestUrl = appInstall.manifest_url.toHttpUrl()
    val manifest = manifestLoader.loadManifest(manifestUrl)
    appLoader.downloadWasm(manifestUrl, manifest)
  }
}
