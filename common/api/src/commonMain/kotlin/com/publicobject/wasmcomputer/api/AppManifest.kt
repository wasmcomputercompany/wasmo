package com.publicobject.wasmcomputer.api

import kotlinx.serialization.Serializable
import okio.ByteString

@Serializable
data class AppManifest(
  val canonicalUrl: String?,
  val version: Long,
  val wasmUrl: String,
  @Serializable(with = ByteStringAsHexSerializer::class)
  val wasmSha256: ByteString,
)
