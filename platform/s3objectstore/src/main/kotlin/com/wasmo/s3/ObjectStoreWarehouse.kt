package com.wasmo.s3

import com.wasmo.ObjectStore
import kotlin.time.Clock
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jaxb3.JaxbConverterFactory
import retrofit2.create

class ObjectStoreWarehouse(
  private val clock: Clock,
  private val client: OkHttpClient,
) {
  fun connect(address: BackblazeB2BucketAddress): ObjectStore {
    val awsRequestSigV4Signer = AwsRequestSigV4Signer(
      clock = clock,
      accessKeyId = address.applicationKeyId,
      secretAccessKey = address.applicationKey,
      region = address.regionId,
      service = "s3",
    )

    val authenticatedHttpClient = client.newBuilder()
      .addNetworkInterceptor(awsRequestSigV4Signer)
      .build()

    val retrofit = Retrofit.Builder()
      .client(authenticatedHttpClient)
      .baseUrl(address.baseUrl)
      .addConverterFactory(JaxbConverterFactory.create())
      .build()

    val simpleStorageService = retrofit.create<SimpleStorageService>()

    return S3ObjectStore(
      service = simpleStorageService,
    )
  }
}

class BackblazeB2BucketAddress(
  val regionId: String,
  val applicationKeyId: String,
  val applicationKey: String,
  val bucket: String,
) {
  val baseUrl: HttpUrl
    get() = "https://s3.${regionId}.backblazeb2.com/$bucket/".toHttpUrl()

  override fun toString() = baseUrl.toString()
}
