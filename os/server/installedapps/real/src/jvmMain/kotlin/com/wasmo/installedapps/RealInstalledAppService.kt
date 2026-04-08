package com.wasmo.installedapps

import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.InstalledAppScope
import com.wasmo.packaging.AppManifest
import com.wasmo.wasm.AppLoader
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okhttp3.HttpUrl
import wasmo.app.Platform
import wasmo.app.WasmoApp

@Inject
@SingleIn(InstalledAppScope::class)
class RealInstalledAppService(
  private val deployment: Deployment,
  private val computerSlug: ComputerSlug,
  private val httpServiceProvider: Lazy<InstalledAppHttpService>,
  private val loader: AppLoader,
  override val slug: AppSlug,
  override val manifest: AppManifest,
  override val platform: Platform,
) : InstalledAppService {
  override val url: HttpUrl
    get() = deployment.baseUrl.newBuilder()
      .host("$slug-$computerSlug.${deployment.baseUrl.host}")
      .build()

  override val homeUrl: HttpUrl
    get() = manifest.launcher?.home_path
      ?.let { url.resolve(it) }
      ?: url

  override val maskableIconUrl: HttpUrl
    get() = manifest.launcher?.maskable_icon_path
      ?.let { url.resolve(it) }
      ?: url.resolve("/maskable-icon.svg")!!

  override val httpService: InstalledAppHttpService
    get() = httpServiceProvider.value

  // TODO: memoize this.
  override suspend fun app(): WasmoApp? =
    loader.load(platform, slug)

  override fun snapshot() = InstalledAppSnapshot(
    slug = slug,
    launcherLabel = manifest.launcher?.label ?: slug.value,
    maskableIconUrl = maskableIconUrl.toString(),
    homeUrl = homeUrl.toString(),
  )
}
