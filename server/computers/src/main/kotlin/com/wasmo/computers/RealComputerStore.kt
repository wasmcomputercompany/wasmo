package com.wasmo.computers

import com.wasmo.api.AppManifest
import com.wasmo.api.WasmoJson
import com.wasmo.app.db.WasmoDbService
import com.wasmo.deployment.Deployment
import com.wasmo.downloader.RealDownloader
import com.wasmo.framework.BadRequestException
import com.wasmo.http.HttpClient
import com.wasmo.identifiers.ComputerId
import com.wasmo.objectstore.ObjectStore
import com.wasmo.objectstore.ScopedObjectStore
import kotlin.time.Clock
import okhttp3.HttpUrl

class RealComputerStore(
  private val deployment: Deployment,
  private val clock: Clock,
  private val rootObjectStore: ObjectStore,
  private val httpClient: HttpClient,
  private val objectStoreKeyFactory: ObjectStoreKeyFactory,
  private val service: WasmoDbService,
) : ComputerStore {
  override fun create(slug: String): WasmoComputer {
    service.transactionWithResult(noEnclosing = true) {
      service.computerQueries.insertComputer(
        created_at = clock.now(),
        slug = slug,
      ).executeAsOne()
    }

    return get(slug)
  }

  override fun get(slug: String): WasmoComputer {
    val computer = service.computerQueries.selectComputerBySlug(
      slug = slug,
    ).executeAsOneOrNull()
      ?: throw BadRequestException("unexpected computer: $slug")

    val objectStore = ScopedObjectStore(
      delegate = rootObjectStore,
      prefix = "$slug/",
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
      service = service,
      computerId = computer.id,
      url = deployment.baseUrl.resolve("/computer/$slug")!!,
      objectStore = objectStore,
      appLoader = appLoader,
    )
  }
}

class RealWasmoComputer(
  private val clock: Clock,
  private val service: WasmoDbService,
  private val computerId: ComputerId,
  override val url: HttpUrl,
  override val objectStore: ObjectStore,
  override val appLoader: AppLoader,
) : WasmoComputer {
  override suspend fun installApp(manifest: AppManifest) {
    service.transactionWithResult(noEnclosing = true) {
      service.appInstallQueries.insertAppInstall(
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
