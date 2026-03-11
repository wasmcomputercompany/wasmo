package com.wasmo.installedapps

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.identifiers.AppSlug
import com.wasmo.packaging.AppManifest
import okhttp3.HttpUrl

interface InstalledAppService {
  val slug: AppSlug
  val manifest: AppManifest
  val url: HttpUrl
  val maskableIconUrl: HttpUrl
  val httpService: InstalledAppHttpService

  context(transactionCallbacks: TransactionCallbacks)
  fun snapshot(): InstalledAppSnapshot

  /**
   * Save all resources listed in the manifest to the object store.
   */
  suspend fun install()
}
