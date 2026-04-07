package com.wasmo.journal.server

import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.JournalJson
import com.wasmo.journal.api.ListEntriesRequest
import com.wasmo.journal.api.ListEntriesResponse
import com.wasmo.journal.api.PublishState
import com.wasmo.journal.api.RequestPublishRequest
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.SaveEntryResponse
import com.wasmo.journal.db.JournalDbService
import com.wasmo.journal.server.attachments.AttachmentStore
import com.wasmo.journal.server.attachments.GetAttachmentAction
import com.wasmo.journal.server.attachments.PostAttachmentAction
import com.wasmo.journal.server.entries.GetEntryAction
import com.wasmo.journal.server.entries.ListEntriesAction
import com.wasmo.journal.server.entries.SaveEntryAction
import com.wasmo.journal.server.publishing.GetPublishStateAction
import com.wasmo.journal.server.publishing.PublishTracker
import com.wasmo.journal.server.publishing.RequestPublishAction
import kotlin.time.Clock
import kotlinx.serialization.serializer
import okio.ByteString.Companion.encodeUtf8
import wasmo.http.Header
import wasmo.http.HttpRequest
import wasmo.http.HttpResponse
import wasmo.http.HttpService
import wasmo.jobs.JobQueue

class JournalHttpService(
  private val clock: Clock,
  private val journalDb: JournalDbService,
  private val attachmentStore: AttachmentStore,
  private val publishSiteJobQueue: JobQueue,
  private val publishTracker: PublishTracker,
) : HttpService {
  fun adminPageAction() = AdminPageAction()

  fun listEntriesAction() = ListEntriesAction(
    journalDb = journalDb,
  )

  fun getEntryAction() = GetEntryAction(
    journalDb = journalDb,
  )

  fun saveEntryAction() = SaveEntryAction(
    clock = clock,
    journalDb = journalDb,
    publishTracker = publishTracker,
  )

  fun getAttachmentAction() = GetAttachmentAction(
    attachmentStore = attachmentStore,
  )

  fun postAttachmentAction() = PostAttachmentAction(
    clock = clock,
    journalDb = journalDb,
    attachmentStore = attachmentStore,
    publishTracker = publishTracker,
  )

  fun getPublishStateAction() = GetPublishStateAction(
    publishTracker = publishTracker,
  )

  fun requestPublishAction() = RequestPublishAction(
    publishSiteJobQueue = publishSiteJobQueue,
    publishTracker = publishTracker,
  )

  override suspend fun execute(request: HttpRequest): HttpResponse {
    if (request.method == "POST") {
      SaveEntryAction.PathRegex.matchEntire(request.url.encodedPath)?.let { match ->
        return postApi<SaveEntryRequest, SaveEntryResponse>(request) { requestBody ->
          saveEntryAction().save(match, requestBody)
        }
      }

      ListEntriesAction.PathRegex.matchEntire(request.url.encodedPath)?.let {
        return postApi<ListEntriesRequest, ListEntriesResponse>(request) {
          listEntriesAction().list(it)
        }
      }

      PostAttachmentAction.PathRegex.matchEntire(request.url.encodedPath)?.let {
        return postAttachmentAction().post(it, request)
      }

      RequestPublishAction.PathRegex.matchEntire(request.url.encodedPath)?.let {
        return postApi<RequestPublishRequest, PublishState>(request) { requestBody ->
          requestPublishAction().requestPublish(requestBody)
        }
      }
    }

    if (request.method == "GET") {
      AdminPageAction.AdminHomePathRegex.matchEntire(request.url.encodedPath)?.let {
        return adminPageAction().admin()
      }

      AdminPageAction.AdminEntryPathRegex.matchEntire(request.url.encodedPath)?.let {
        return adminPageAction().admin()
      }

      GetEntryAction.PathRegex.matchEntire(request.url.encodedPath)?.let { match ->
        return getApi<EntrySnapshot> {
          getEntryAction().get(match)
        }
      }

      GetAttachmentAction.PathRegex.matchEntire(request.url.encodedPath)?.let {
        return getAttachmentAction().get(it)
      }

      GetPublishStateAction.PathRegex.matchEntire(request.url.encodedPath)?.let { match ->
        return getApi<PublishState> {
          getPublishStateAction().get(match)
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
}
