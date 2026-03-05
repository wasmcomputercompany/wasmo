package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSlug
import com.wasmo.api.ComputerSnapshot
import com.wasmo.db.AppInstall
import com.wasmo.identifiers.ComputerId
import okhttp3.HttpUrl

interface WasmoComputer {
  val id: ComputerId
  val slug: ComputerSlug
  val url: HttpUrl

  /** Install default apps. */
  context(transactionCallbacks: TransactionCallbacks)
  fun initialize()

  context(transactionCallbacks: TransactionCallbacks)
  fun snapshot(): ComputerSnapshot

  suspend fun enqueueInstallApp(manifestUrl: HttpUrl)

  suspend fun enqueueInstallApp(appInstall: AppInstall)
}
