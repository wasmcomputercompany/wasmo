package com.wasmo.s3

import com.wasmo.DeleteObjectRequest
import com.wasmo.DeleteObjectResponse
import com.wasmo.GetObjectRequest
import com.wasmo.GetObjectResponse
import com.wasmo.ListObjectsRequest
import com.wasmo.ListObjectsResponse
import com.wasmo.ObjectStore
import com.wasmo.PutObjectRequest
import com.wasmo.PutObjectResponse
import kotlinx.serialization.json.Json
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal class S3ObjectStore(
  private val service: SimpleStorageService,
  private val bucket: String,
) : ObjectStore {
  override suspend fun put(request: PutObjectRequest): PutObjectResponse {
    TODO("Not yet implemented")
  }

  override suspend fun get(request: GetObjectRequest): GetObjectResponse {
    TODO("Not yet implemented")
  }

  override suspend fun delete(request: DeleteObjectRequest): DeleteObjectResponse {
    TODO("Not yet implemented")
  }

  override suspend fun list(request: ListObjectsRequest): ListObjectsResponse {
    val string = service.list(
      bucket = bucket,
      delimiter = request.delimiter,
      prefix = request.prefix,
      continuationToken = request.continuationToken,
    )
    println(string)
    return ListObjectsResponse(
      entries = listOf(),
      nextRequest = null,
    )
  }
}

/**
 * https://www.backblaze.com/apidocs/introduction-to-the-s3-compatible-api
 */
internal interface SimpleStorageService {
  @GET("/{bucket}/?list-type=2")
  suspend fun list(
    @Path("bucket") bucket: String,
    @Query("delimiter") delimiter: String?,
    @Query("prefix") prefix: String?,
    @Query("continuation-token") continuationToken: String?,
  ): String
}

internal val S3Json = Json {
  this.ignoreUnknownKeys = true
}
