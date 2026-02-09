package com.wasmo.admin.server

import com.wasmo.HttpClient
import com.wasmo.WasmoApp
import com.wasmo.admin.api.AdminJson
import com.wasmo.admin.db.AdminDbService
import kotlin.time.Clock
import okio.Closeable

class AdminWasmoApp(
  private val clock: Clock,
  private val httpClient: HttpClient,
  private val install: WasmoApp.Install,
) : Closeable, WasmoApp {
  private var adminDbService_: AdminDbService? = null

  val adminDbService: AdminDbService
    get() {
      return adminDbService_
        ?: AdminDbService.open(
          path = install.dataDirectory / "AdminDb.db",
        ).also {
          adminDbService_ = it
        }
    }

  fun installAppAction() = InstallAppAction(
    clock = clock,
    appLoader = AppLoader(
      json = AdminJson,
      httpClient = httpClient,
    ),
    adminDbService = adminDbService,
  )

  override fun afterInstall(
    oldVersion: Long,
    newVersion: Long,
  ) {
    adminDbService.migrate()
  }

  override fun close() {
    adminDbService_?.close()
  }
}
