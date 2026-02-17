package com.wasmo.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import okio.ByteString
import okio.ByteString.Companion.decodeBase64

val WasmoJson = Json {
  this.ignoreUnknownKeys = true
}

object Base64UrlSerializer : KSerializer<ByteString> {
  private val delegateSerializer = String.serializer()
  override val descriptor = SerialDescriptor("okio.ByteString", delegateSerializer.descriptor)

  override fun serialize(encoder: Encoder, value: ByteString) {
    encoder.encodeSerializableValue(delegateSerializer, value.base64Url())
  }

  override fun deserialize(decoder: Decoder): ByteString {
    val string = decoder.decodeSerializableValue(delegateSerializer)
    return string.decodeBase64() ?: throw SerializationException("base64 decode failed")
  }
}

