package com.wasmo.hello.server

import com.wasmo.hello.db.HelloDbService
import com.wasmo.sqldelight.driver
import kotlin.time.Clock
import okio.ByteString.Companion.encodeUtf8
import okio.Closeable
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.http.HttpRequest
import wasmo.http.HttpResponse
import wasmo.http.HttpService

class HelloWasmoApp(
  private val clock: Clock,
  private val helloDb: HelloDbService,
) : Closeable, WasmoApp, HttpService {
  override val httpService: HttpService
    get() = this

  fun greetAction() = GreetAction(
    clock = clock,
    helloDb = helloDb,
  )

  override suspend fun afterInstall(
    oldVersion: Long,
    newVersion: Long,
  ) {
    helloDb.migrate()
  }

  override suspend fun execute(request: HttpRequest): HttpResponse {
    return HttpResponse(
      body = "hello world".encodeUtf8(),
    )
  }

  override fun close() {
    helloDb.close()
  }

  class Factory : WasmoApp.Factory {
    override suspend fun create(platform: Platform): HelloWasmoApp {
      val dbService = HelloDbService(
        driver = platform.sqlService.getOrCreate().driver(),
      )
      return HelloWasmoApp(
        clock = platform.clock,
        helloDb = dbService,
      )
    }
  }
}
