package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.api.ComputerSlug
import com.wasmo.db.AppInstall
import com.wasmo.identifiers.ComputerId
import okhttp3.HttpUrl

interface WasmoComputer {
  val url: HttpUrl
  val appLoader: AppLoader

  suspend fun enqueueInstallApp(manifestUrl: HttpUrl)

  suspend fun enqueueInstallApp(appInstall: AppInstall)
}

interface ComputerStore {
  context(transactionCallbacks: TransactionCallbacks)
  fun getOrNull(client: Client, slug: ComputerSlug): WasmoComputer?

  context(transactionCallbacks: TransactionCallbacks)
  fun get(computerId: ComputerId): WasmoComputer
}
