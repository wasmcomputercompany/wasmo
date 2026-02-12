package com.wasmo.s3

import com.wasmo.ObjectStore
import kotlin.time.Clock
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jaxb3.JaxbConverterFactory
import retrofit2.create

data class B2Region(val id: String)

fun connectB2(
  clock: Clock,
  client: OkHttpClient,
  region: B2Region,
  applicationKeyId: String,
  applicationKey: String,
  bucket: String,
): ObjectStore {
  val awsRequestSigV4Signer = AwsRequestSigV4Signer(
    clock = clock,
    accessKeyId = applicationKeyId,
    secretAccessKey = applicationKey,
    region = region.id,
    service = "s3",
  )
  val authenticatedClient = client.newBuilder()
    .addNetworkInterceptor(awsRequestSigV4Signer)
    .build()

  val retrofit = Retrofit.Builder()
    .client(authenticatedClient)
    .baseUrl("https://s3.${region.id}.backblazeb2.com/")
    .addConverterFactory(JaxbConverterFactory.create())
    .build()

  val simpleStorageService = retrofit.create<SimpleStorageService>()
  return S3ObjectStore(
    service = simpleStorageService,
    bucket = bucket,
  )
}
