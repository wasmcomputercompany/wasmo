package com.wasmo.s3

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Converter
import retrofit2.converter.jaxb3.JaxbConverterFactory

class XmlCodecTest {
  private val converterFactory = JaxbConverterFactory.create()

  @Test
  fun happyPath() {
    val converter = converterFactory.responseBodyConverter(
      ListBucketResult::class.java,
      arrayOf(),
      null,
    ) as Converter<ResponseBody, ListBucketResult>

    val xml =
      """
      |<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      |<ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
      |    <Contents>
      |        <ETag>"85f30635602dc09bd85957a6e82a2c21"</ETag>
      |        <Key>object-1</Key>
      |        <LastModified>2022-01-28T23:24:07.000Z</LastModified>
      |        <Size>11</Size>
      |        <StorageClass>STANDARD</StorageClass>
      |    </Contents>
      |    <Contents>
      |        <ETag>"85f30635602dc09bd85957a6e82a2c21"</ETag>
      |        <Key>object-2</Key>
      |        <LastModified>2022-01-28T23:25:45.000Z</LastModified>
      |        <Size>11</Size>
      |        <StorageClass>STANDARD</StorageClass>
      |    </Contents>
      |    <Contents>
      |        <ETag>"3729bf569924c845dfe90bfe6281a9ff-1"</ETag>
      |        <Key>object-3</Key>
      |        <LastModified>2022-01-28T23:27:20.000Z</LastModified>
      |        <Size>11</Size>
      |        <StorageClass>STANDARD</StorageClass>
      |    </Contents>
      |    <IsTruncated>false</IsTruncated>
      |    <MaxKeys>1000</MaxKeys>
      |    <Name>my-bucket-name</Name>
      |    <Prefix></Prefix>
      |    <KeyCount>3</KeyCount>
      |</ListBucketResult>
      """.trimMargin()

    assertThat(converter.convert(xml.toResponseBody()))
      .isEqualTo(
        ListBucketResult(
          Contents = mutableListOf(
            Contents(
              ETag = "\"85f30635602dc09bd85957a6e82a2c21\"",
              Key = "object-1",
              LastModified = "2022-01-28T23:24:07.000Z",
              Size = 11,
              StorageClass = "STANDARD",
            ),
            Contents(
              ETag = "\"85f30635602dc09bd85957a6e82a2c21\"",
              Key = "object-2",
              LastModified = "2022-01-28T23:25:45.000Z",
              Size = 11,
              StorageClass = "STANDARD",
            ),
            Contents(
              ETag = "\"3729bf569924c845dfe90bfe6281a9ff-1\"",
              Key = "object-3",
              LastModified = "2022-01-28T23:27:20.000Z",
              Size = 11,
              StorageClass = "STANDARD",
            ),
          ),
          IsTruncated = false,
          MaxKeys = 1000,
          Name = "my-bucket-name",
          Prefix = "",
          KeyCount = 3,
        ),
      )
  }
}
