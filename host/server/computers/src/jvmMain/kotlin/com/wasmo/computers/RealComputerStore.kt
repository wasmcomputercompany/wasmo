package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.api.AppManifest
import com.wasmo.api.ComputerSlug
import com.wasmo.db.Computer
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.downloader.RealDownloader
import com.wasmo.framework.BadRequestException
import com.wasmo.identifiers.ComputerId
import com.wasmo.jobs.JobQueue
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import okhttp3.HttpUrl
import wasmo.http.HttpClient
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.ScopedObjectStore

@Inject
@SingleIn(AppScope::class)
class RealComputerStore(
  private val deployment: Deployment,
  private val clock: Clock,
  private val rootObjectStore: ObjectStore,
  private val httpClient: HttpClient,
  private val objectStoreKeyFactory: ObjectStoreKeyFactory,
  private val wasmoDb: WasmoDb,
  private val installAppJobQueue: JobQueue<InstallAppJob>,
) : ComputerStore {
  context(transactionCallbacks: TransactionCallbacks)
  override fun get(
    client: Client,
    slug: ComputerSlug,
  ): WasmoComputer {
    val accountId = client.getAccountIdOrNull()
      ?: throw BadRequestException("unexpected computer: $slug")

    val computer = wasmoDb.computerQueries.selectComputerByAccountIdAndSlug(
      account_id = accountId,
      slug = slug,
    ).executeAsOneOrNull()
      ?: throw BadRequestException("unexpected computer: $slug")

    return get(computer)
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun get(computerId: ComputerId): WasmoComputer {
    val computer = wasmoDb.computerQueries.selectComputerById(
      id = computerId,
    ).executeAsOne()

    return get(computer)
  }

  private fun get(
    computer: Computer,
  ): RealWasmoComputer {
    val objectStore = ScopedObjectStore(
      delegate = rootObjectStore,
      prefix = "${computer.slug.value}/",
    )
    val downloader = RealDownloader(
      httpClient = httpClient,
      objectStore = objectStore,
    )
    val appLoader = AppLoader(
      downloader = downloader,
      objectStoreKeyFactory = objectStoreKeyFactory,
    )
    return RealWasmoComputer(
      clock = clock,
      wasmoDb = wasmoDb,
      computerId = computer.id,
      url = deployment.baseUrl.resolve("/computer/${computer.slug.value}")!!,
      objectStore = objectStore,
      appLoader = appLoader,
      installAppJobQueue = installAppJobQueue,
    )
  }
}

class RealWasmoComputer(
  private val clock: Clock,
  private val wasmoDb: WasmoDb,
  private val computerId: ComputerId,
  override val url: HttpUrl,
  override val objectStore: ObjectStore,
  override val appLoader: AppLoader,
  private val installAppJobQueue: JobQueue<InstallAppJob>,
) : WasmoComputer {
  context(transactionCallbacks: TransactionCallbacks)
  override fun installApp(manifestUrl: String, manifest: AppManifest) {
    val appInstallId = wasmoDb.appInstallQueries.insertAppInstall(
      computer_id = computerId,
      slug = manifest.slug,
      manifest_url = manifest.canonicalUrl ?: manifestUrl,
      display_name = manifest.displayName,
      version = manifest.version,
      install_scheduled_at = clock.now(),
    ).executeAsOne()

    installAppJobQueue.enqueue(InstallAppJob(appInstallId))
  }
}
