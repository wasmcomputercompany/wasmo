package com.wasmo.computers

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.Path
import okio.Path.Companion.toPath

sealed interface ManifestAddress {
  data class Http(val url: HttpUrl) : ManifestAddress {
    override fun toString() = url.toString()
  }

  data class FileSystem(val path: Path) : ManifestAddress {
    override fun toString() = path.toString()
  }

  companion object {
    fun String.toManifestAddress(): ManifestAddress {
      val httpUrl = toHttpUrlOrNull()
      return when {
        httpUrl != null -> Http(httpUrl)
        else -> FileSystem(toPath())
      }
    }
  }
}
