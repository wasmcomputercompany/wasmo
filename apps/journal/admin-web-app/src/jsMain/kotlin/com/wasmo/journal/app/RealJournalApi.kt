package com.wasmo.journal.app

import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.JournalJson
import com.wasmo.journal.api.ListEntriesRequest
import com.wasmo.journal.api.ListEntriesResponse
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.SaveEntryResponse
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.serializer

@OptIn(ExperimentalSerializationApi::class)
class RealJournalApi : JournalApi {
  override suspend fun listEntries(
    request: ListEntriesRequest,
  ): ListEntriesResponse = post("/api/entries", request)

  override suspend fun getEntry(
    token: String,
  ): EntrySnapshot = get("/api/entries/$token")

  override suspend fun saveEntry(
    token: String,
    request: SaveEntryRequest,
  ): SaveEntryResponse = post("/api/entries/$token", request)

  suspend inline fun <reified S, reified R> post(
    path: String,
    request: S,
  ): R {
    val requestSerializer = serializer<S>()
    val responseDeserializer = serializer<R>()

    val requestJson = JournalJson.encodeToString(requestSerializer, request)
    val request = js(
      """
      {
        method: "POST",
        body: requestJson,
      }
      """,
    )
    val response = window.fetch(path, request).await()

    if (response.status !in 200..<300) {
      throw Exception("unexpected response code: ${response.status}")
    }

    val json = response.json().await()
    return JournalJson.decodeFromDynamic(responseDeserializer, json)
  }

  suspend inline fun <reified R> get(path: String): R {
    val responseDeserializer = serializer<R>()
    val request = js(
      """
      {
        method: "GET",
      }
      """,
    )
    val response = window.fetch(path, request).await()

    if (response.status !in 200..<300) {
      throw Exception("unexpected response code: ${response.status}")
    }

    val json = response.json().await()
    return JournalJson.decodeFromDynamic(responseDeserializer, json)
  }
}
