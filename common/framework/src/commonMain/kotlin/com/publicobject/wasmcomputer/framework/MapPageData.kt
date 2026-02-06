package com.publicobject.wasmcomputer.framework

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

class MapPageData(
  internal val json: Json,
  internal val map: Map<String, JsonElement>,
) : PageData() {
  override fun <T> get(key: String, serializer: KSerializer<T>): T? {
    val element = map[key] ?: return null
    return json.decodeFromJsonElement(serializer, element)
  }

  class Builder(
    @PublishedApi
    internal val json: Json,
  ) {
    @PublishedApi
    internal val map = mutableMapOf<String, JsonElement>()

    inline fun <reified T> put(key: String, value: T) = apply {
      map[key] = json.encodeToJsonElement(value)
    }

    fun build() = MapPageData(json, map.toMap())
  }
}
