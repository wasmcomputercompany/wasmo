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
import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlRootElement
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

internal class S3ObjectStore(
  private val service: SimpleStorageService,
  private val bucket: String,
) : ObjectStore {
  override suspend fun put(request: PutObjectRequest): PutObjectResponse {
    val response = service.put(
      bucketAndKey = "${bucket}/${request.key}",
      requestBody = request.value.toRequestBody(),
    )
    return PutObjectResponse(
      etag = response.headers()["ETag"]!!,
    )
  }

  override suspend fun get(request: GetObjectRequest): GetObjectResponse {
    val response = service.get(
      bucketAndKey = "${bucket}/${request.key}",
    )
    return GetObjectResponse(
      etag = response.headers()["ETag"]!!,
      value = response.body()!!.byteString(),
    )
  }

  override suspend fun delete(request: DeleteObjectRequest): DeleteObjectResponse {
    service.delete(
      bucketAndKey = "${bucket}/${request.key}",
    )
    return DeleteObjectResponse
  }

  override suspend fun list(request: ListObjectsRequest): ListObjectsResponse {
    val listBucketResult = service.list(
      bucket = bucket,
      delimiter = request.delimiter,
      prefix = request.prefix,
      continuationToken = request.continuationToken,
    )
    return ListObjectsResponse(
      entries = listBucketResult.Contents.map {
        ListObjectsResponse.Object(
          key = it.Key,
          etag = it.ETag,
          size = it.Size,
        )
      },
      nextRequest = listBucketResult.NextContinuationToken?.let {
        request.copy(continuationToken = it)
      },
    )
  }
}

/**
 * https://www.backblaze.com/apidocs/introduction-to-the-s3-compatible-api
 */
internal interface SimpleStorageService {
  @PUT
  suspend fun put(
    @Url bucketAndKey: String,
    @Body requestBody: RequestBody,
  ): Response<Unit>

  @GET
  suspend fun get(
    @Url bucketAndKey: String,
  ): Response<ResponseBody>

  @DELETE
  suspend fun delete(
    @Url bucketAndKey: String,
  ): Response<Unit>

  @GET("/{bucket}/?list-type=2")
  suspend fun list(
    @Path("bucket") bucket: String,
    @Query("delimiter") delimiter: String?,
    @Query("prefix") prefix: String?,
    @Query("continuation-token") continuationToken: String?,
  ): ListBucketResult
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
data class ListBucketResult(
  var Contents: MutableList<Contents> = mutableListOf(),
  var IsTruncated: Boolean? = null,
  var MaxKeys: Int? = null,
  var Name: String? = null,
  var Prefix: String? = null,
  var KeyCount: Int? = null,
  var NextContinuationToken: String? = null,
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Contents(
  var ETag: String,
  var Key: String,
  var LastModified: String? = null,
  var Size: Long,
  var StorageClass: String? = null,
)
