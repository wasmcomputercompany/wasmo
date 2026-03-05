package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSlug
import com.wasmo.db.AppInstall
import com.wasmo.identifiers.ComputerId
import okhttp3.HttpUrl

interface WasmoComputer {
  val id: ComputerId
  val slug: ComputerSlug
  val url: HttpUrl
  val appLoader: AppLoader

  /** Install default apps. */
  context(transactionCallbacks: TransactionCallbacks)
  fun initialize()

  suspend fun enqueueInstallApp(manifestUrl: HttpUrl)

  suspend fun enqueueInstallApp(appInstall: AppInstall)
}

