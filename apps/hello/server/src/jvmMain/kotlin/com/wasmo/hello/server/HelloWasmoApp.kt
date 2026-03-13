package com.wasmo.hello.server

import com.wasmo.hello.db.HelloDbService
import okio.Closeable
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.http.HttpService

class HelloWasmoApp(
  private val platform: Platform,
  private val helloDb: HelloDbService,
) : Closeable, WasmoApp {
  override val httpService: HttpService?
    get() = null

  fun greetAction() = GreetAction(
    clock = platform.clock,
    helloDb = helloDb,
  )

  override suspend fun afterInstall(
    oldVersion: Long,
    newVersion: Long,
  ) {
    helloDb.migrate()
  }

  override fun close() {
    helloDb.close()
  }
}
