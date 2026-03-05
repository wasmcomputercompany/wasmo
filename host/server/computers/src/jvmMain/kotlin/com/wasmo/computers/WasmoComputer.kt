package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.api.AppManifest
import com.wasmo.api.ComputerSlug
import com.wasmo.identifiers.ComputerId
import okhttp3.HttpUrl
import wasmo.objectstore.ObjectStore

interface WasmoComputer {
  val url: HttpUrl
  val objectStore: ObjectStore
  val appLoader: AppLoader

  context(transactionCallbacks: TransactionCallbacks)
  fun installApp(manifestUrl: String, manifest: AppManifest)
}

interface ComputerStore {
  context(transactionCallbacks: TransactionCallbacks)
  fun get(client: Client, slug: ComputerSlug): WasmoComputer

  context(transactionCallbacks: TransactionCallbacks)
  fun get(computerId: ComputerId): WasmoComputer
}
