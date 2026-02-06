package app.rounds.framework

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

/**
 * JSON data embedded in the HTML page to save an API call.
 */
abstract class PageData {
  inline fun <reified T> get(key: String): T? {
    return get(key, serializer<T>())
  }

  abstract fun <T> get(key: String, serializer: KSerializer<T>): T?
}
