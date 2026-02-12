package com.wasmo.objectstore

import com.wasmo.objectstore.filesystem.FileSystemObjectStore
import com.wasmo.objectstore.s3.S3Client
import kotlin.time.Clock
import okhttp3.OkHttpClient

class ObjectStoreFactory(
  private val clock: Clock,
  private val client: OkHttpClient,
) {
  private val s3Client = S3Client(
    clock = clock,
    client = client,
  )

  fun open(address: ObjectStoreAddress): ObjectStore {
    return when (address) {
      is BackblazeB2BucketAddress -> s3Client.connect(address)
      is FileSystemObjectStoreAddress -> FileSystemObjectStore(address.fileSystem, address.path)
    }
  }
}
