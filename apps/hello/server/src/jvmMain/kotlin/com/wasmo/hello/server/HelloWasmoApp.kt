package com.wasmo.hello.server

import com.wasmo.app.WasmoApp
import com.wasmo.hello.db.HelloDbService
import kotlin.time.Clock
import okio.Closeable

class HelloWasmoApp(
  private val clock: Clock,
  private val install: WasmoApp.Install,
) : Closeable, WasmoApp {
  private var helloDbService_: HelloDbService? = null

  val helloDbService: HelloDbService
    get() {
      return helloDbService_
        ?: HelloDbService.open(
          path = install.dataDirectory / "HelloDb.db",
        ).also {
          helloDbService_ = it
        }
    }

  fun greetAction() = GreetAction(
    clock = clock,
    helloDbService = helloDbService,
  )

  override fun afterInstall(
    oldVersion: Long,
    newVersion: Long,
  ) {
    helloDbService.migrate()
  }

  override fun close() {
    helloDbService_?.close()
  }
}
