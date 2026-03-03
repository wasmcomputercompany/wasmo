package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.api.AppManifest
import com.wasmo.api.ComputerSlug
import com.wasmo.api.WasmoJson
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.downloader.RealDownloader
import com.wasmo.framework.BadRequestException
import com.wasmo.http.HttpClient
import com.wasmo.identifiers.ComputerId
import com.wasmo.objectstore.ObjectStore
import com.wasmo.objectstore.ScopedObjectStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import okhttp3.HttpUrl

@Inject
@SingleIn(AppScope::class)
class RealComputerStore(
  private val deployment: Deployment,
  private val clock: Clock,
  private val rootObjectStore: ObjectStore,
  private val httpClient: HttpClient,
  private val objectStoreKeyFactory: ObjectStoreKeyFactory,
  private val wasmoDb: WasmoDb,
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

    val objectStore = ScopedObjectStore(
      delegate = rootObjectStore,
      prefix = "${slug.value}/",
    )
    val downloader = RealDownloader(
      httpClient = httpClient,
      objectStore = objectStore,
    )
    val appLoader = AppLoader(
      json = WasmoJson,
      httpClient = httpClient,
      downloader = downloader,
      objectStoreKeyFactory = objectStoreKeyFactory,
    )
    return RealWasmoComputer(
      clock = clock,
      wasmoDb = wasmoDb,
      computerId = computer.id,
      url = deployment.baseUrl.resolve("/computer/$slug")!!,
      objectStore = objectStore,
      appLoader = appLoader,
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
) : WasmoComputer {
  override suspend fun installApp(manifest: AppManifest) {
    wasmoDb.transactionWithResult(noEnclosing = true) {
      wasmoDb.appInstallQueries.insertAppInstall(
        created_at = clock.now(),
        computer_id = computerId,
        slug = manifest.slug,
        display_name = manifest.displayName,
        version = manifest.version,
      ).executeAsOne()
    }

    appLoader.downloadWasm(manifest)
  }
}
