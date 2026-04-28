package com.wasmo.installedapps

import com.wasmo.accounts.Client
import com.wasmo.db.computers.DbComputer
import com.wasmo.db.computers.DbComputerAccess
import com.wasmo.db.computers.selectComputer
import com.wasmo.db.computers.selectComputerAndComputerAccess
import com.wasmo.db.computers.selectComputerByAccountIdAndSlug
import com.wasmo.db.computers.selectComputerById
import com.wasmo.db.installedapps.DbInstalledApp
import com.wasmo.db.installedapps.DbInstalledAppRelease
import com.wasmo.db.installedapps.selectInstalledAppByComputerIdAndSlug
import com.wasmo.db.installedapps.selectInstalledAppById
import com.wasmo.db.installedapps.selectInstalledAppReleaseById
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.OsScope
import com.wasmo.sql.SqlTransaction
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.access.Caller
import wasmo.access.ComputerAccess

@Inject
@SingleIn(OsScope::class)
class RealInstalledAppStore(
  private val installedAppServiceGraphFactory: InstalledAppServiceGraph.Factory,
  private val appManifestLoaderFactory: RealAppManifestLoaderFactory,
) : InstalledAppStore {
  context(sqlTransaction: SqlTransaction)
  override suspend fun getHttpServiceAndAccessOrNull(
    client: Client,
    computerSlug: ComputerSlug,
    appSlug: AppSlug,
  ): Pair<InstalledAppHttpService, Caller>? {
    val accountId = client.getAccountIdOrNull()

    val (computer: DbComputer, caller: Caller) = when {
      accountId == null -> {
        val computer = selectComputer(computerSlug) ?: return null
        computer to createCaller(client, null)
      }

      else -> {
        val (computer, access) = selectComputerAndComputerAccess(accountId, computerSlug)
          ?: return null
        computer to createCaller(client, access)
      }
    }

    val row = selectInstalledAppByComputerIdAndSlug(
      computerId = computer.id,
      slug = appSlug,
      active = true,
    ) ?: return null

    val installedAppService = get(
      computerSlug = computer.slug,
      installedApp = row.installedApp,
      installedAppRelease = row.installedAppRelease,
    )

    return installedAppService.httpService to caller
  }

  private fun createCaller(
    client: Client,
    dbComputerAccess: DbComputerAccess?,
  ) = Caller(
    userId = dbComputerAccess?.userId?.id,
    computerAccess = when {
      dbComputerAccess != null -> ComputerAccess.Owner
      else -> ComputerAccess.Anonymous
    },
    userAgent = client.userAgent,
    ip = client.ip,
  )

  context(sqlTransaction: SqlTransaction)
  override suspend fun getOrNull(
    client: Client,
    computerSlug: ComputerSlug,
    appSlug: AppSlug,
  ): InstalledAppService? {
    val accountId = client.getAccountIdOrNull()
      ?: return null

    val computer = selectComputerByAccountIdAndSlug(
      accountId = accountId,
      slug = computerSlug,
    ) ?: return null

    val row = selectInstalledAppByComputerIdAndSlug(
      computerId = computer.id,
      slug = appSlug,
      active = true,
    ) ?: return null

    return get(
      computerSlug = computer.slug,
      installedApp = row.installedApp,
      installedAppRelease = row.installedAppRelease,
    )
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun get(installedAppId: InstalledAppId): InstalledAppService? {
    val installedApp = selectInstalledAppById(installedAppId)
    val installedAppRelease = selectInstalledAppReleaseById(
      id = installedApp.activeReleaseId ?: return null,
    )
    return get(
      installedApp = installedApp,
      installedAppRelease = installedAppRelease,
    )
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun get(
    installedApp: DbInstalledApp,
    installedAppRelease: DbInstalledAppRelease?,
  ): InstalledAppService {
    val computer = selectComputerById(installedApp.computerId)
    return get(computer.slug, installedApp, installedAppRelease)
  }

  override suspend fun get(
    computerSlug: ComputerSlug,
    installedApp: DbInstalledApp,
    installedAppRelease: DbInstalledAppRelease?,
  ): InstalledAppService {
    val appManifestLoader = appManifestLoaderFactory.create(
      installedApp = installedApp,
      installedAppRelease = installedAppRelease,
    )
    val graph = installedAppServiceGraphFactory.create(
      computerSlug = computerSlug,
      installedApp = installedApp,
      appManifestLoader = appManifestLoader,
    )
    return graph.service
  }
}
