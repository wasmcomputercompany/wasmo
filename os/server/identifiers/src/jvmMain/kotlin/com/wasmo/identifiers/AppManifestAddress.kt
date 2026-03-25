package com.wasmo.identifiers

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.Path
import okio.Path.Companion.toPath

sealed interface AppManifestAddress {
  data class Http(val url: HttpUrl) : AppManifestAddress {
    override fun toString() = url.toString()
  }

  data class FileSystem(val path: Path) : AppManifestAddress {
    /** Returns the path to resolve resources against. */
    val basePath: Path
      get() = path.parent!!

    override fun toString() = path.toString()
  }

  companion object {
    fun String.toAppManifestAddress(): AppManifestAddress {
      val httpUrl = toHttpUrlOrNull()
      return when {
        httpUrl != null -> Http(httpUrl)
        else -> FileSystem(toPath())
      }
    }
  }
}
