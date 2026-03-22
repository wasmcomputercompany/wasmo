package com.wasmo.objectstore.s3

import com.wasmo.objectstore.BackblazeB2BucketAddress
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreConnector
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import wasmo.objectstore.ObjectStore

@BindingContainer
object S3ObjectStoreBindings {
  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  internal fun provideObjectStoreConnector(
    s3Client: S3Client,
  ): ObjectStoreConnector = object : ObjectStoreConnector {
    override fun tryConnect(address: ObjectStoreAddress): ObjectStore? {
      if (address !is BackblazeB2BucketAddress) return null
      return s3Client.connect(address)
    }
  }
}
