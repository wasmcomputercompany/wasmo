package com.wasmo.journal.server

import com.wasmo.journal.api.GreetRequest
import com.wasmo.journal.api.GreetResponse
import com.wasmo.journal.api.JournalJson
import com.wasmo.journal.db.JournalDbService
import com.wasmo.sqldelight.driver
import kotlin.time.Clock
import okio.ByteString.Companion.encodeUtf8
import okio.Closeable
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.http.Header
import wasmo.http.HttpRequest
import wasmo.http.HttpResponse
import wasmo.http.HttpService

class JournalWasmoApp(
  private val clock: Clock,
  private val journalDb: JournalDbService,
) : Closeable, WasmoApp, HttpService {
  override val httpService: HttpService
    get() = this

  fun greetAction() = GreetAction(
    clock = clock,
    journalDb = journalDb,
  )

  override suspend fun afterInstall(
    oldVersion: Long,
    newVersion: Long,
  ) {
    journalDb.migrate()
  }

  override suspend fun execute(request: HttpRequest): HttpResponse {
    return when (request.url.encodedPath) {
      "/" -> HomeAction().home()

      "/greet" -> {
        val request = JournalJson.decodeFromString(
          GreetRequest.serializer(),
          request.body!!.utf8(),
        )
        val response = GreetAction(clock, journalDb).greet(request)
        val responseBody = JournalJson.encodeToString(
          GreetResponse.serializer(),
          response,
        )
        HttpResponse(
          body = responseBody.encodeUtf8(),
        )
      }

      else -> {
        HttpResponse(
          headers = listOf(
            Header("content-type", "text/html"),
          ),
          body = "hello world".encodeUtf8(),
        )
      }
    }
  }

  override fun close() {
    journalDb.close()
  }

  class Factory : WasmoApp.Factory {
    override suspend fun create(platform: Platform): JournalWasmoApp {
      val dbService = JournalDbService(
        driver = platform.sqlService.getOrCreate().driver(),
      )
      return JournalWasmoApp(
        clock = platform.clock,
        journalDb = dbService,
      )
    }
  }
}
