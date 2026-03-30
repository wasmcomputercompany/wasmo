package com.wasmo.journal.server

import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.JournalJson
import com.wasmo.journal.api.ListEntriesRequest
import com.wasmo.journal.api.ListEntriesResponse
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.SaveEntryResponse
import com.wasmo.journal.db.JournalDbService
import com.wasmo.journal.server.admin.GetEntryAction
import com.wasmo.journal.server.admin.ListEntriesAction
import com.wasmo.journal.server.admin.SaveEntryAction
import com.wasmo.journal.server.attachments.GetAttachmentAction
import com.wasmo.journal.server.attachments.PostAttachmentAction
import com.wasmo.sqldelight.driver
import kotlin.time.Clock
import kotlinx.serialization.serializer
import okio.ByteString.Companion.encodeUtf8
import okio.Closeable
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.http.Header
import wasmo.http.HttpRequest
import wasmo.http.HttpResponse
import wasmo.http.HttpService
import wasmo.objectstore.ObjectStore

class JournalWasmoApp(
  private val clock: Clock,
  private val objectStore: ObjectStore,
  private val journalDb: JournalDbService,
) : Closeable, WasmoApp, HttpService {
  override val httpService: HttpService
    get() = this

  fun homePage() = HomePage()

  fun listEntriesAction() = ListEntriesAction(
    journalDb = journalDb,
  )

  fun getEntryAction() = GetEntryAction(
    journalDb = journalDb,
  )

  fun saveEntryAction() = SaveEntryAction(
    journalDb = journalDb,
  )

  fun getAttachmentAction() = GetAttachmentAction(
    objectStore = objectStore,
  )

  fun postAttachmentAction() = PostAttachmentAction(
    clock = clock,
    journalDb = journalDb,
    objectStore = objectStore,
  )

  override suspend fun afterInstall(
    oldVersion: Long,
    newVersion: Long,
  ) {
    journalDb.migrate()
  }

  override suspend fun execute(request: HttpRequest): HttpResponse {
    if (request.method == "POST") {
      val saveEntryMatch = SaveEntryAction.PathRegex.matchEntire(request.url.encodedPath)
      if (saveEntryMatch != null) {
        return postApi<SaveEntryRequest, SaveEntryResponse>(request) { requestBody ->
          saveEntryAction().save(saveEntryMatch, requestBody)
        }
      }

      val listEntriesMatch = ListEntriesAction.PathRegex.matchEntire(request.url.encodedPath)
      if (listEntriesMatch != null) {
        return postApi<ListEntriesRequest, ListEntriesResponse>(request) {
          listEntriesAction().list(it)
        }
      }
    }

    if (request.method == "GET") {
      val homeMatch = HomePage.PathRegex.matchEntire(request.url.encodedPath)
      if (homeMatch != null) {
        return homePage().home()
      }

      val getEntryMatch = GetEntryAction.PathRegex.matchEntire(request.url.encodedPath)
      if (getEntryMatch != null) {
        return getApi<EntrySnapshot> {
          getEntryAction().get(getEntryMatch)
        }
      }
    }

    return HttpResponse(
      code = 404,
      headers = listOf(
        Header("content-type", "text/html"),
      ),
      body = "not found".encodeUtf8(),
    )
  }

  private inline fun <reified S> getApi(block: () -> S): HttpResponse {
    val response = block()
    val responseBody = JournalJson.encodeToString(serializer<S>(), response)
    return HttpResponse(
      body = responseBody.encodeUtf8(),
    )
  }

  private inline fun <reified R, reified S> postApi(
    request: HttpRequest,
    block: (R) -> S,
  ): HttpResponse {
    val request = JournalJson.decodeFromString(serializer<R>(), request.body!!.utf8())
    val response = block(request)
    val responseBody = JournalJson.encodeToString(serializer<S>(), response)
    return HttpResponse(
      body = responseBody.encodeUtf8(),
    )
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
        objectStore = platform.objectStore,
        journalDb = dbService,
      )
    }
  }
}
