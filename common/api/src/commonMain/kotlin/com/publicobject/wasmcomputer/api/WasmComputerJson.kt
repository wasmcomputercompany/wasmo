package com.publicobject.wasmcomputer.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import okio.ByteString
import okio.ByteString.Companion.decodeHex

val WasmComputerJson = Json {
  this.ignoreUnknownKeys = true
}

object ByteStringAsHexSerializer : KSerializer<ByteString> {
  override val descriptor = PrimitiveSerialDescriptor("ByteString", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ByteString) {
    encoder.encodeString(value.hex())
  }

  override fun deserialize(decoder: Decoder): ByteString {
    return decoder.decodeString().decodeHex()
  }
}
