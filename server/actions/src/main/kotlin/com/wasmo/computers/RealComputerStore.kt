package com.wasmo.computers

import com.wasmo.HttpClient
import com.wasmo.ObjectStore
import com.wasmo.RealDownloader
import com.wasmo.ScopedObjectStore
import com.wasmo.api.AppManifest
import com.wasmo.api.WasmComputerJson
import com.wasmo.app.db.WasmoDbService
import com.wasmo.apps.AppLoader
import com.wasmo.apps.ObjectStoreKeyFactory
import com.wasmo.framework.BadRequestException
import com.wasmo.identifiers.ComputerId
import kotlin.time.Clock
import okhttp3.HttpUrl

class RealComputerStore(
  private val baseUrl: HttpUrl,
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
      json = WasmComputerJson,
      httpClient = httpClient,
      downloader = downloader,
      objectStoreKeyFactory = objectStoreKeyFactory,
    )
    return RealWasmoComputer(
      clock = clock,
      service = service,
      computerId = computer.id,
      url = baseUrl.resolve("/computer/$slug")!!,
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
