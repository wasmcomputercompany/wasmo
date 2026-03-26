package com.wasmo.identifiers

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.Path
import okio.Path.Companion.toPath

sealed interface WasmoFileAddress {
  data class Http(val url: HttpUrl) : WasmoFileAddress {
    override fun toString() = url.toString()
  }

  data class FileSystem(val path: Path) : WasmoFileAddress {
    override fun toString() = path.toString()
  }

  companion object {
    fun String.toWasmoFileAddress(): WasmoFileAddress {
      val httpUrl = toHttpUrlOrNull()
      return when {
        httpUrl != null -> Http(httpUrl)
        else -> FileSystem(toPath())
      }
    }
  }
}
