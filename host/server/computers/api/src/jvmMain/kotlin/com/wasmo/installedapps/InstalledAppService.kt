package com.wasmo.installedapps

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.identifiers.AppSlug
import com.wasmo.packaging.AppManifest
import okhttp3.HttpUrl

interface InstalledAppService {
  val slug: AppSlug
  val manifest: AppManifest
  val url: HttpUrl
  val maskableIconUrl: HttpUrl

  context(transactionCallbacks: TransactionCallbacks)
  fun snapshot(): InstalledAppSnapshot

  /**
   * Save all resources listed in the manifest to the object store.
   */
  suspend fun install()

  suspend fun call(request: Request): Response<ResponseBody>
}
