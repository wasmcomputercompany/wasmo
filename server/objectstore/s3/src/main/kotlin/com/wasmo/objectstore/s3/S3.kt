package com.wasmo.objectstore.s3

import com.wasmo.objectstore.BackblazeB2BucketAddress
import com.wasmo.objectstore.DeleteObjectRequest
import com.wasmo.objectstore.DeleteObjectResponse
import com.wasmo.objectstore.GetObjectRequest
import com.wasmo.objectstore.GetObjectResponse
import com.wasmo.objectstore.ListObjectsRequest
import com.wasmo.objectstore.ListObjectsResponse
import com.wasmo.objectstore.ObjectStore
import com.wasmo.objectstore.PutObjectRequest
import com.wasmo.objectstore.PutObjectResponse
import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlRootElement
import kotlin.time.Clock
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jaxb3.JaxbConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Url

class S3Client(
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

internal class S3ObjectStore(
  private val service: SimpleStorageService,
) : ObjectStore {
  override suspend fun put(request: PutObjectRequest): PutObjectResponse {
    val response = service.put(
      key = request.key,
      requestBody = request.value.toRequestBody(),
    )
    return PutObjectResponse(
      etag = response.headers()["ETag"]!!,
    )
  }

  override suspend fun get(request: GetObjectRequest): GetObjectResponse {
    val response = service.get(
      key = request.key,
    )
    return GetObjectResponse(
      etag = response.headers()["ETag"]!!,
      value = response.body()!!.byteString(),
    )
  }

  override suspend fun delete(request: DeleteObjectRequest): DeleteObjectResponse {
    service.delete(
      key = request.key,
    )
    return DeleteObjectResponse
  }

  override suspend fun list(request: ListObjectsRequest): ListObjectsResponse {
    val listBucketResult = service.list(
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
    @Url key: String,
    @Body requestBody: RequestBody,
  ): Response<Unit>

  @GET
  suspend fun get(
    @Url key: String,
  ): Response<ResponseBody>

  @DELETE
  suspend fun delete(
    @Url key: String,
  ): Response<Unit>

  @GET("?list-type=2")
  suspend fun list(
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
