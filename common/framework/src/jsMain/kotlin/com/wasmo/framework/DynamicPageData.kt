package com.wasmo.framework

import kotlinx.browser.document
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic

internal class DynamicPageData(
  val json: Json,
  val pageData: dynamic,
) : PageData() {
  @OptIn(ExperimentalSerializationApi::class)
  override fun <T> get(key: String, serializer: KSerializer<T>): T? {
    val jsonValue = pageData[key] ?: return null
    return json.decodeFromDynamic(serializer, jsonValue)
  }

  companion object {
    val Empty: PageData = DynamicPageData(Json, js("{}"))
  }
}

fun detectPageData(json: Json): PageData {
  val pageData = document.asDynamic().pageData
  return when {
    jsTypeOf(pageData) == "object" -> DynamicPageData(json, pageData)
    else -> DynamicPageData.Empty
  }
}
