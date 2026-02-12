package com.wasmo.s3

import com.wasmo.ObjectStore
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

class B2Region private constructor(val id: String) {
  companion object {
    val CaEast = B2Region("ca-east")
  }
}

fun connectB2(
  client: OkHttpClient,
  region: B2Region,
  applicationKeyId: String,
  applicationKey: String,
  bucket: String,
): ObjectStore {
  val retrofit = Retrofit.Builder()
    .client(client)
    .baseUrl("https://s3.${region.id}.backblazeb2.com/")
    .addConverterFactory(S3Json.asConverterFactory("application/json; charset=utf-8".toMediaType()))
    .build()

  val simpleStorageService = retrofit.create<SimpleStorageService>()
  return S3ObjectStore(
    service = simpleStorageService,
    bucket = bucket,
  )
}
