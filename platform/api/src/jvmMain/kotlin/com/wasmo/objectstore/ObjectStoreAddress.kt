package com.wasmo.objectstore

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.FileSystem
import okio.Path

sealed interface ObjectStoreAddress

data class BackblazeB2BucketAddress(
  val regionId: String,
  val applicationKeyId: String,
  val applicationKey: String,
  val bucket: String,
) : ObjectStoreAddress {
  val baseUrl: HttpUrl
    get() = "https://s3.${regionId}.backblazeb2.com/$bucket/".toHttpUrl()

  override fun toString() = baseUrl.toString()
}

data class FileSystemObjectStoreAddress(
  val fileSystem: FileSystem,
  val path: Path,
) : ObjectStoreAddress {
  override fun toString() = path.toString()
}
