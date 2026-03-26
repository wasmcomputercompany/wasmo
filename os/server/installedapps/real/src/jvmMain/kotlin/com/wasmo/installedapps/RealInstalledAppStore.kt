package com.wasmo.installedapps

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.db.InstalledApp
import com.wasmo.db.WasmoDb
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class RealInstalledAppStore(
  private val wasmoDb: WasmoDb,
  private val installedAppServiceGraphFactory: InstalledAppServiceGraph.Factory,
) : InstalledAppStore {

  context(transactionCallbacks: TransactionCallbacks)
  override fun getOrNull(
    client: Client,
    computerSlug: ComputerSlug,
    appSlug: AppSlug,
  ): InstalledAppService? {
    val accountId = client.getAccountIdOrNull()
      ?: return null

    val computer = wasmoDb.computerQueries.selectComputerByAccountIdAndSlug(
      account_id = accountId,
      slug = computerSlug,
    ).executeAsOneOrNull()
      ?: return null

    val installedApp = wasmoDb.installedAppQueries.selectInstalledAppByComputerIdAndSlug(
      computer_id = computer.id,
      slug = appSlug,
      active = true,
    ).executeAsOneOrNull()
      ?: return null

    return get(computer.slug, installedApp)
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun get(installedApp: InstalledApp): InstalledAppService {
    val computer = wasmoDb.computerQueries.selectComputerById(installedApp.computer_id)
      .executeAsOne()
    return get(computer.slug, installedApp)
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun get(computerSlug: ComputerSlug, installedApp: InstalledApp): InstalledAppService {
    val graph = installedAppServiceGraphFactory.create(computerSlug, installedApp)
    return graph.service
  }
}
