package com.wasmo.passkeys

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.api.PasskeyAuthentication
import com.wasmo.api.PasskeyRegistration
import com.wasmo.api.WasmoJson
import kotlin.test.Test
import okio.ByteString.Companion.decodeBase64

class PasskeysJsonTest {
  @Test
  fun registrationDocumentedSampleValue() {
    val json = """
      |{
      |  "type": "public-key",
      |  "id": "L10e_letUcC5nKayX3rMWN_aBY1pyDQfsPb4Tq_8vIo",
      |  "rawId": "L10e_letUcC5nKayX3rMWN_aBY1pyDQfsPb4Tq_8vIo=",
      |  "authenticatorAttachment": "cross-platform",
      |  "clientExtensionResults": {},
      |  "response": {
      |    "attestationObject": "o2NmbXRmcGFja2VkZ2F0dFN0bXSjY2FsZyZjc2lnWEYwRAIgWyQipZIS0ocYBL-UX9xGXhI60li5U7wmxlRbDtWFkHsCIDlkKM-_pqOTAWPrJJ0x_Q4eLsYEKneQFIJou6qyaC9eY3g1Y4FZAdgwggHUMIIBeqADAgECAgEBMAoGCCqGSM49BAMCMGAxCzAJBgNVBAYTAlVTMREwDwYDVQQKDAhDaHJvbWl1bTEiMCAGA1UECwwZQXV0aGVudGljYXRvciBBdHRlc3RhdGlvbjEaMBgGA1UEAwwRQmF0Y2ggQ2VydGlmaWNhdGUwHhcNMTcwNzE0MDI0MDAwWhcNNDUwNjIwMDgzOTEyWjBgMQswCQYDVQQGEwJVUzERMA8GA1UECgwIQ2hyb21pdW0xIjAgBgNVBAsMGUF1dGhlbnRpY2F0b3IgQXR0ZXN0YXRpb24xGjAYBgNVBAMMEUJhdGNoIENlcnRpZmljYXRlMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEjWF-ZclQjmS8xWc6yCpnmdo8FEZoLCWMRj__31jf0vo-bDeLU9eVxKTf-0GZ7deGLyOrrwIDtLiRG6BWmZThAaMlMCMwDAYDVR0TAQH_BAIwADATBgsrBgEEAYLlHAIBAQQEAwIFIDAKBggqhkjOPQQDAgNIADBFAiEAwxO40zwx1TBr4V05D_cHqdaCrThO-ZyyIzkgH9QcBpICICrerGEd6cX8PY0ipJPOkjkEWR8SwbxQjjm57cVsVT5uaGF1dGhEYXRhWKRPsghW8kpq59r8J4EJCshHeubivQcmYCNsxhTG-3wuoEUAAAABAQIDBAUGBwgBAgMEBQYHCAAgL10e_letUcC5nKayX3rMWN_aBY1pyDQfsPb4Tq_8vIqlAQIDJiABIVggxZ3bQ9Xa4kbrskcobWw8Drr4Facf074_o9GbN_g56L8iWCAV2IOeHxBrcNS35XQFD1VoGWiO0Aqx3QnmVst3aCz6rg==",
      |    "authenticatorData": "T7IIVvJKaufa_CeBCQrIR3rm4r0HJmAjbMYUxvt8LqBFAAAAAQECAwQFBgcIAQIDBAUGBwgAIC9dHv5XrVHAuZymsl96zFjf2gWNacg0H7D2-E6v_LyKpQECAyYgASFYIMWd20PV2uJG67JHKG1sPA66-BWnH9O-P6PRmzf4Oei_IlggFdiDnh8Qa3DUt-V0BQ9VaBlojtAKsd0J5lbLd2gs-q4=",
      |    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoibHpwQnRUaFFyQUttckxOYWFidHRZbGpaIiwib3JpZ2luIjoiaHR0cHM6Ly93ZWJhdXRobi5wYXNzd29yZGxlc3MuaWQiLCJjcm9zc09yaWdpbiI6ZmFsc2V9",
      |    "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAExZ3bQ9Xa4kbrskcobWw8Drr4Facf074_o9GbN_g56L8V2IOeHxBrcNS35XQFD1VoGWiO0Aqx3QnmVst3aCz6rg==",
      |    "publicKeyAlgorithm": -7,
      |    "transports": [
      |      "usb"
      |    ]
      |  },
      |  "user": {
      |    "name": "Arnaud",
      |    "id": "4aa4df5f-1666-4400-aca5-3b627cc9008a"
      |  }
      |}
      """.trimMargin()
    val value = PasskeyRegistration(
      type = "public-key",
      response = PasskeyRegistration.Response(
        attestationObject = "o2NmbXRmcGFja2VkZ2F0dFN0bXSjY2FsZyZjc2lnWEYwRAIgWyQipZIS0ocYBL-UX9xGXhI60li5U7wmxlRbDtWFkHsCIDlkKM-_pqOTAWPrJJ0x_Q4eLsYEKneQFIJou6qyaC9eY3g1Y4FZAdgwggHUMIIBeqADAgECAgEBMAoGCCqGSM49BAMCMGAxCzAJBgNVBAYTAlVTMREwDwYDVQQKDAhDaHJvbWl1bTEiMCAGA1UECwwZQXV0aGVudGljYXRvciBBdHRlc3RhdGlvbjEaMBgGA1UEAwwRQmF0Y2ggQ2VydGlmaWNhdGUwHhcNMTcwNzE0MDI0MDAwWhcNNDUwNjIwMDgzOTEyWjBgMQswCQYDVQQGEwJVUzERMA8GA1UECgwIQ2hyb21pdW0xIjAgBgNVBAsMGUF1dGhlbnRpY2F0b3IgQXR0ZXN0YXRpb24xGjAYBgNVBAMMEUJhdGNoIENlcnRpZmljYXRlMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEjWF-ZclQjmS8xWc6yCpnmdo8FEZoLCWMRj__31jf0vo-bDeLU9eVxKTf-0GZ7deGLyOrrwIDtLiRG6BWmZThAaMlMCMwDAYDVR0TAQH_BAIwADATBgsrBgEEAYLlHAIBAQQEAwIFIDAKBggqhkjOPQQDAgNIADBFAiEAwxO40zwx1TBr4V05D_cHqdaCrThO-ZyyIzkgH9QcBpICICrerGEd6cX8PY0ipJPOkjkEWR8SwbxQjjm57cVsVT5uaGF1dGhEYXRhWKRPsghW8kpq59r8J4EJCshHeubivQcmYCNsxhTG-3wuoEUAAAABAQIDBAUGBwgBAgMEBQYHCAAgL10e_letUcC5nKayX3rMWN_aBY1pyDQfsPb4Tq_8vIqlAQIDJiABIVggxZ3bQ9Xa4kbrskcobWw8Drr4Facf074_o9GbN_g56L8iWCAV2IOeHxBrcNS35XQFD1VoGWiO0Aqx3QnmVst3aCz6rg==".decodeBase64()!!,
        clientDataJSON = "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoibHpwQnRUaFFyQUttckxOYWFidHRZbGpaIiwib3JpZ2luIjoiaHR0cHM6Ly93ZWJhdXRobi5wYXNzd29yZGxlc3MuaWQiLCJjcm9zc09yaWdpbiI6ZmFsc2V9".decodeBase64()!!,
        publicKeyAlgorithm = -7,
        transports = listOf("usb"),
      ),
    )
    assertThat(WasmoJson.decodeFromString<PasskeyRegistration>(json)).isEqualTo(value)
  }

  @Test
  fun registrationFrom1Password() {
    val json = """
      |{
      |  "type": "public-key",
      |  "id": "7SxKiGoyIXwwTmnFU6aQJAvy5lJxENJYIf66dTmaYM2TF4xsGiMQe2c",
      |  "rawId": "7SxKiGoyIXwwTmnFU6aQJAvy5lJxENJYIf66dTmaYM2TF4xsGiMQe2c=",
      |  "authenticatorAttachment": "platform",
      |  "clientExtensionResults": {},
      |  "response": {
      |    "attestationObject": "o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YVitSZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2NdAAAAALraVWanqkAfvZZFYZpVEg0AKe0sSohqMiF8ME5pxVOmkCQL8uZScRDSWCH-unU5mmDNkxeMbBojEHtnpQECAyYgASFYICdhwyWliikHle5sVdfdGPxmSL-dmYrIA78mwxTGxG_RIlggZlQgnMczJeX5mlJIxaHOX4ZXXjWYsGJ1dk-_DYSpSxk=",
      |    "authenticatorData": "SZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2NdAAAAALraVWanqkAfvZZFYZpVEg0AKe0sSohqMiF8ME5pxVOmkCQL8uZScRDSWCH-unU5mmDNkxeMbBojEHtnpQECAyYgASFYICdhwyWliikHle5sVdfdGPxmSL-dmYrIA78mwxTGxG_RIlggZlQgnMczJeX5mlJIxaHOX4ZXXjWYsGJ1dk-_DYSpSxk=",
      |    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImNyb3NzT3JpZ2luIjpmYWxzZX0=",
      |    "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEJ2HDJaWKKQeV7mxV190Y_GZIv52ZisgDvybDFMbEb9FmVCCcxzMl5fmaUkjFoc5fhldeNZiwYnV2T78NhKlLGQ==",
      |    "publicKeyAlgorithm": -7,
      |    "transports": [
      |      "internal",
      |      "hybrid"
      |    ]
      |  },
      |  "user": {
      |    "name": "passkeys@rounds.app",
      |    "id": "c188393a-b938-47f1-9d56-9f50ed95b1b2"
      |  }
      |}
      """.trimMargin()
    val value = PasskeyRegistration(
      type = "public-key",
      response = PasskeyRegistration.Response(
        attestationObject = "o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YVitSZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2NdAAAAALraVWanqkAfvZZFYZpVEg0AKe0sSohqMiF8ME5pxVOmkCQL8uZScRDSWCH-unU5mmDNkxeMbBojEHtnpQECAyYgASFYICdhwyWliikHle5sVdfdGPxmSL-dmYrIA78mwxTGxG_RIlggZlQgnMczJeX5mlJIxaHOX4ZXXjWYsGJ1dk-_DYSpSxk=".decodeBase64()!!,
        clientDataJSON = "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImNyb3NzT3JpZ2luIjpmYWxzZX0=".decodeBase64()!!,
        publicKeyAlgorithm = -7,
        transports = listOf("internal", "hybrid"),
      ),
    )
    assertThat(WasmoJson.decodeFromString<PasskeyRegistration>(json)).isEqualTo(value)
  }

  @Test
  fun registrationFromIcloudKeychain() {
    val json = """
      |{
      |  "type": "public-key",
      |  "id": "WFnvosrYnoWySyCvuaehSLKMWkg",
      |  "rawId": "WFnvosrYnoWySyCvuaehSLKMWkg=",
      |  "authenticatorAttachment": "platform",
      |  "clientExtensionResults": {},
      |  "response": {
      |    "attestationObject": "o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YViYlRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpldAAAAAPv8MAcVTk7MjAtuAgVX170AFFhZ76LK2J6Fsksgr7mnoUiyjFpIpQECAyYgASFYIMFvlEjaa_E7F3UpB-1Hyj68M8sn162-PIaZ5AV3ChtvIlggnLqZTXoTOBmLMXSjp5eSaP0UAVW29ymilpTvdQicFVM=",
      |    "authenticatorData": "lRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpldAAAAAPv8MAcVTk7MjAtuAgVX170AFFhZ76LK2J6Fsksgr7mnoUiyjFpIpQECAyYgASFYIMFvlEjaa_E7F3UpB-1Hyj68M8sn162-PIaZ5AV3ChtvIlggnLqZTXoTOBmLMXSjp5eSaP0UAVW29ymilpTvdQicFVM=",
      |    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==",
      |    "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEwW-USNpr8TsXdSkH7UfKPrwzyyfXrb48hpnkBXcKG2-cuplNehM4GYsxdKOnl5Jo_RQBVbb3KaKWlO91CJwVUw==",
      |    "publicKeyAlgorithm": -7,
      |    "transports": [
      |      "hybrid",
      |      "internal"
      |    ]
      |  },
      |  "user": {
      |    "name": "passkeys@rounds.app",
      |    "id": "298ac031-d6de-41ff-a7f5-a81686f1c433"
      |  }
      |}
      """.trimMargin()
    val value = PasskeyRegistration(
      type = "public-key",
      response = PasskeyRegistration.Response(
        attestationObject = "o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YViYlRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpldAAAAAPv8MAcVTk7MjAtuAgVX170AFFhZ76LK2J6Fsksgr7mnoUiyjFpIpQECAyYgASFYIMFvlEjaa_E7F3UpB-1Hyj68M8sn162-PIaZ5AV3ChtvIlggnLqZTXoTOBmLMXSjp5eSaP0UAVW29ymilpTvdQicFVM=".decodeBase64()!!,
        clientDataJSON = "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==".decodeBase64()!!,
        publicKeyAlgorithm = -7,
        transports = listOf("hybrid", "internal"),
      ),
    )
    assertThat(WasmoJson.decodeFromString<PasskeyRegistration>(json)).isEqualTo(value)
  }

  @Test
  fun registrationFromIcloudKeychainWithSafari() {
    val json = """
      |{
      |  "type": "public-key",
      |  "id": "DfjtVlcfi8dHwCANVMW1vnQLzDM",
      |  "rawId": "DfjtVlcfi8dHwCANVMW1vnQLzDM=",
      |  "authenticatorAttachment": "platform",
      |  "clientExtensionResults": {},
      |  "response": {
      |    "attestationObject": "o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YViYlRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpldAAAAAPv8MAcVTk7MjAtuAgVX170AFA347VZXH4vHR8AgDVTFtb50C8wzpQECAyYgASFYIKucoS3lrE10VrsdP1B_eAPAnp7G3aMcrGoXN1wso2KuIlggsCTLk758Oi-AXOJitVOj77qpK8_t0npr03jSI4ac08Y=",
      |    "authenticatorData": "lRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpldAAAAAPv8MAcVTk7MjAtuAgVX170AFA347VZXH4vHR8AgDVTFtb50C8wzpQECAyYgASFYIKucoS3lrE10VrsdP1B_eAPAnp7G3aMcrGoXN1wso2KuIlggsCTLk758Oi-AXOJitVOj77qpK8_t0npr03jSI4ac08Y=",
      |    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==",
      |    "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEq5yhLeWsTXRWux0_UH94A8Censbdoxysahc3XCyjYq6wJMuTvnw6L4Bc4mK1U6Pvuqkrz-3SemvTeNIjhpzTxg==",
      |    "publicKeyAlgorithm": -7,
      |    "transports": [
      |      "internal",
      |      "hybrid"
      |    ]
      |  },
      |  "user": {
      |    "name": "passkeys@rounds.app",
      |    "id": "0d3887a9-a0bc-463b-8bc9-460af275a624"
      |  }
      |}
      """.trimMargin()
    val value = PasskeyRegistration(
      type = "public-key",
      response = PasskeyRegistration.Response(
        attestationObject = "o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YViYlRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpldAAAAAPv8MAcVTk7MjAtuAgVX170AFA347VZXH4vHR8AgDVTFtb50C8wzpQECAyYgASFYIKucoS3lrE10VrsdP1B_eAPAnp7G3aMcrGoXN1wso2KuIlggsCTLk758Oi-AXOJitVOj77qpK8_t0npr03jSI4ac08Y=".decodeBase64()!!,
        clientDataJSON = "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==".decodeBase64()!!,
        publicKeyAlgorithm = -7,
        transports = listOf("internal", "hybrid"),
      ),
    )
    assertThat(WasmoJson.decodeFromString<PasskeyRegistration>(json)).isEqualTo(value)
  }

  @Test
  fun registrationFromChromeProfile() {
    val json = """
      |{
      |  "type": "public-key",
      |  "id": "pnU-oA-_FYSsdMZaRkawuo7P9rC91AU8SOFV5oQHNqU",
      |  "rawId": "pnU-oA-_FYSsdMZaRkawuo7P9rC91AU8SOFV5oQHNqU=",
      |  "authenticatorAttachment": "platform",
      |  "clientExtensionResults": {},
      |  "response": {
      |    "attestationObject": "o2NmbXRmcGFja2VkZ2F0dFN0bXSiY2FsZyZjc2lnWEcwRQIhAJyGpCrkWE58usNHK3KGC_pQlFYCUQ9KUOy92xSWDqpEAiAzyQf9TRYgxDlbuB5Yz8cUC31KEO57amc0Ow17K6AcgmhhdXRoRGF0YViklRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LplFAAAAAK3OAAI1vMYKZIsLJfHwVQMAIKZ1PqAPvxWErHTGWkZGsLqOz_awvdQFPEjhVeaEBzalpQECAyYgASFYIJ4ywAmIYTlaD6uzgBWA3bWA4IuZWb_bRnf68y1I2E_tIlggj3dzk2jCexCcPjgICA2kSR6d2maepH88c1AyqfZwf6Y=",
      |    "authenticatorData": "lRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LplFAAAAAK3OAAI1vMYKZIsLJfHwVQMAIKZ1PqAPvxWErHTGWkZGsLqOz_awvdQFPEjhVeaEBzalpQECAyYgASFYIJ4ywAmIYTlaD6uzgBWA3bWA4IuZWb_bRnf68y1I2E_tIlggj3dzk2jCexCcPjgICA2kSR6d2maepH88c1AyqfZwf6Y=",
      |    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==",
      |    "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEnjLACYhhOVoPq7OAFYDdtYDgi5lZv9tGd_rzLUjYT-2Pd3OTaMJ7EJw-OAgIDaRJHp3aZp6kfzxzUDKp9nB_pg==",
      |    "publicKeyAlgorithm": -7,
      |    "transports": [
      |      "internal"
      |    ]
      |  },
      |  "user": {
      |    "name": "passkeys@rounds.app",
      |    "id": "c164b47c-0f8d-4c01-8a8a-9492c46f025f"
      |  }
      |}
      """.trimMargin()
    val value = PasskeyRegistration(
      type = "public-key",
      response = PasskeyRegistration.Response(
        attestationObject = "o2NmbXRmcGFja2VkZ2F0dFN0bXSiY2FsZyZjc2lnWEcwRQIhAJyGpCrkWE58usNHK3KGC_pQlFYCUQ9KUOy92xSWDqpEAiAzyQf9TRYgxDlbuB5Yz8cUC31KEO57amc0Ow17K6AcgmhhdXRoRGF0YViklRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LplFAAAAAK3OAAI1vMYKZIsLJfHwVQMAIKZ1PqAPvxWErHTGWkZGsLqOz_awvdQFPEjhVeaEBzalpQECAyYgASFYIJ4ywAmIYTlaD6uzgBWA3bWA4IuZWb_bRnf68y1I2E_tIlggj3dzk2jCexCcPjgICA2kSR6d2maepH88c1AyqfZwf6Y=".decodeBase64()!!,
        clientDataJSON = "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==".decodeBase64()!!,
        publicKeyAlgorithm = -7,
        transports = listOf("internal"),
      ),
    )
    assertThat(WasmoJson.decodeFromString<PasskeyRegistration>(json)).isEqualTo(value)
  }

  @Test
  fun authenticationDocumentedSampleValue() {
    val json = """
      |{
      |  "clientExtensionResults": {},
      |  "id": "L10e_letUcC5nKayX3rMWN_aBY1pyDQfsPb4Tq_8vIo",
      |  "rawId": "L10e_letUcC5nKayX3rMWN_aBY1pyDQfsPb4Tq_8vIo=",
      |  "type": "public-key",
      |  "authenticatorAttachment": "cross-platform",
      |  "response": {
      |    "authenticatorData": "T7IIVvJKaufa_CeBCQrIR3rm4r0HJmAjbMYUxvt8LqAFAAAAAg==",
      |    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoibVRJX1BUNU9NcmlSTWxqNEtZd244NlZ3Iiwib3JpZ2luIjoiaHR0cHM6Ly93ZWJhdXRobi5wYXNzd29yZGxlc3MuaWQiLCJjcm9zc09yaWdpbiI6ZmFsc2UsIm90aGVyX2tleXNfY2FuX2JlX2FkZGVkX2hlcmUiOiJkbyBub3QgY29tcGFyZSBjbGllbnREYXRhSlNPTiBhZ2FpbnN0IGEgdGVtcGxhdGUuIFNlZSBodHRwczovL2dvby5nbC95YWJQZXgifQ==",
      |    "signature": "MEYCIQC2W8TArZgiDw-D6HM526Dfr5uROPwWaCp7f4Lt-R_UHQIhANVdFi7nmHc_ZtMWLwRTHfT8AQF8ssrUFTcroocPra9B",
      |    "userHandle": "NGFhNGRmNWYtMTY2Ni00NDAwLWFjYTUtM2I2MjdjYzkwMDhh"
      |  }
      |}
      """.trimMargin()
    val value = PasskeyAuthentication(
      id = "L10e_letUcC5nKayX3rMWN_aBY1pyDQfsPb4Tq_8vIo",
      response = PasskeyAuthentication.Response(
        authenticatorData = "T7IIVvJKaufa_CeBCQrIR3rm4r0HJmAjbMYUxvt8LqAFAAAAAg==".decodeBase64()!!,
        clientDataJSON = "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoibVRJX1BUNU9NcmlSTWxqNEtZd244NlZ3Iiwib3JpZ2luIjoiaHR0cHM6Ly93ZWJhdXRobi5wYXNzd29yZGxlc3MuaWQiLCJjcm9zc09yaWdpbiI6ZmFsc2UsIm90aGVyX2tleXNfY2FuX2JlX2FkZGVkX2hlcmUiOiJkbyBub3QgY29tcGFyZSBjbGllbnREYXRhSlNPTiBhZ2FpbnN0IGEgdGVtcGxhdGUuIFNlZSBodHRwczovL2dvby5nbC95YWJQZXgifQ==".decodeBase64()!!,
        signature = "MEYCIQC2W8TArZgiDw-D6HM526Dfr5uROPwWaCp7f4Lt-R_UHQIhANVdFi7nmHc_ZtMWLwRTHfT8AQF8ssrUFTcroocPra9B".decodeBase64()!!,
      ),
    )
    assertThat(WasmoJson.decodeFromString<PasskeyAuthentication>(json)).isEqualTo(value)
  }

  @Test
  fun authenticationFrom1Password() {
    val json = """
      |{
      |  "clientExtensionResults": {},
      |  "id": "6HUDLZ_5Rz5lYa3kZOWYB_nK1lPtk-m73PBET7vOp6MWvasHSQ4eXDem",
      |  "rawId": "6HUDLZ_5Rz5lYa3kZOWYB_nK1lPtk-m73PBET7vOp6MWvasHSQ4eXDem",
      |  "type": "public-key",
      |  "authenticatorAttachment": "platform",
      |  "response": {
      |    "authenticatorData": "SZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2MdAAAAAA==",
      |    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImNyb3NzT3JpZ2luIjpmYWxzZX0=",
      |    "signature": "MEUCIQDT1d3kwUFUmVSdfncfkpuw6Y9NyELfvaXVBcOvYCKbJgIgLjqvErLYBsPaJknMkvkXxZq9ioqYHY8uoyHcm0wJ-qM=",
      |    "userHandle": "BSW8eVpjTke30Vl-JfXKug=="
      |  }
      |}
      """.trimMargin()
    val value = PasskeyAuthentication(
      id = "6HUDLZ_5Rz5lYa3kZOWYB_nK1lPtk-m73PBET7vOp6MWvasHSQ4eXDem",
      response = PasskeyAuthentication.Response(
        authenticatorData = "SZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2MdAAAAAA==".decodeBase64()!!,
        clientDataJSON = "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImNyb3NzT3JpZ2luIjpmYWxzZX0=".decodeBase64()!!,
        signature = "MEUCIQDT1d3kwUFUmVSdfncfkpuw6Y9NyELfvaXVBcOvYCKbJgIgLjqvErLYBsPaJknMkvkXxZq9ioqYHY8uoyHcm0wJ-qM=".decodeBase64()!!,
      ),
    )
    assertThat(WasmoJson.decodeFromString<PasskeyAuthentication>(json)).isEqualTo(value)
  }

  @Test
  fun authenticationFromIcloudKeychain() {
    val json = """
      |{
      |  "clientExtensionResults": {},
      |  "id": "WFnvosrYnoWySyCvuaehSLKMWkg",
      |  "rawId": "WFnvosrYnoWySyCvuaehSLKMWkg=",
      |  "type": "public-key",
      |  "authenticatorAttachment": "platform",
      |  "response": {
      |    "authenticatorData": "lRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpkdAAAAAA==",
      |    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==",
      |    "signature": "MEUCIE5NleTIK3laJUsdAjYMDj1loA_ycrAHn9jKsuvw7pwwAiEA3Q3N4hdlwDLx5tw2BnnvtoiOoKvsLlN19r1LRoWKgck=",
      |    "userHandle": "Mjk4YWMwMzEtZDZkZS00MWZmLWE3ZjUtYTgxNjg2ZjFjNDMz"
      |  }
      |}
      """.trimMargin()
    val value = PasskeyAuthentication(
      id = "WFnvosrYnoWySyCvuaehSLKMWkg",
      response = PasskeyAuthentication.Response(
        authenticatorData = "lRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpkdAAAAAA==".decodeBase64()!!,
        clientDataJSON = "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==".decodeBase64()!!,
        signature = "MEUCIE5NleTIK3laJUsdAjYMDj1loA_ycrAHn9jKsuvw7pwwAiEA3Q3N4hdlwDLx5tw2BnnvtoiOoKvsLlN19r1LRoWKgck=".decodeBase64()!!,
      ),
    )
    assertThat(WasmoJson.decodeFromString<PasskeyAuthentication>(json)).isEqualTo(value)
  }

  @Test
  fun authenticationFromIcloudSafari() {
    val json = """
      |{
      |  "clientExtensionResults": {},
      |  "id": "DfjtVlcfi8dHwCANVMW1vnQLzDM",
      |  "rawId": "DfjtVlcfi8dHwCANVMW1vnQLzDM=",
      |  "type": "public-key",
      |  "authenticatorAttachment": "platform",
      |  "response": {
      |    "authenticatorData": "lRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpkdAAAAAA==",
      |    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==",
      |    "signature": "MEUCIQCMJlqDUh9CHMs2Rn5JF4G_ZpHMFkYPtRaJqW62P1gQIQIgHcAY2rrv77u7P4xXAMK75OZ9bh0eOwKVLX9dWXGEXMM=",
      |    "userHandle": "MGQzODg3YTktYTBiYy00NjNiLThiYzktNDYwYWYyNzVhNjI0"
      |  }
      |}
      """.trimMargin()
    val value = PasskeyAuthentication(
      id = "DfjtVlcfi8dHwCANVMW1vnQLzDM",
      response = PasskeyAuthentication.Response(
        authenticatorData = "lRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpkdAAAAAA==".decodeBase64()!!,
        clientDataJSON = "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==".decodeBase64()!!,
        signature = "MEUCIQCMJlqDUh9CHMs2Rn5JF4G_ZpHMFkYPtRaJqW62P1gQIQIgHcAY2rrv77u7P4xXAMK75OZ9bh0eOwKVLX9dWXGEXMM=".decodeBase64()!!,
      ),
    )
    assertThat(WasmoJson.decodeFromString<PasskeyAuthentication>(json)).isEqualTo(value)
  }

  @Test
  fun authenticationFromChromeProfile() {
    val json = """
      |{
      |  "clientExtensionResults": {},
      |  "id": "pnU-oA-_FYSsdMZaRkawuo7P9rC91AU8SOFV5oQHNqU",
      |  "rawId": "pnU-oA-_FYSsdMZaRkawuo7P9rC91AU8SOFV5oQHNqU=",
      |  "type": "public-key",
      |  "authenticatorAttachment": "platform",
      |  "response": {
      |    "authenticatorData": "lRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpkFAAAAAA==",
      |    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==",
      |    "signature": "MEUCIQCPX73coxP5-sZW91dLAfuQIXrXQ-4vDwHPXoqLb2xmgQIgTbaeMAFE06WNGDGQOVGwVsz_p6wsqTOapGt5CllIFgU=",
      |    "userHandle": "YzE2NGI0N2MtMGY4ZC00YzAxLThhOGEtOTQ5MmM0NmYwMjVm"
      |  }
      |}
      """.trimMargin()
    val value = PasskeyAuthentication(
      id = "pnU-oA-_FYSsdMZaRkawuo7P9rC91AU8SOFV5oQHNqU",
      response = PasskeyAuthentication.Response(
        authenticatorData = "lRwdg_Qv3wPpXPafqvFua6OCGygy_0_-X6Gd1Rc7LpkFAAAAAA==".decodeBase64()!!,
        clientDataJSON = "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiUVNCelpYSjJaWEl0YzJsa1pTQnlZVzVrYjIxc2VTQm5aVzVsY21GMFpXUWdjM1J5YVc1biIsIm9yaWdpbiI6Imh0dHBzOi8vc3RhZ2luZy5yb3VuZHMuYXBwIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==".decodeBase64()!!,
        signature = "MEUCIQCPX73coxP5-sZW91dLAfuQIXrXQ-4vDwHPXoqLb2xmgQIgTbaeMAFE06WNGDGQOVGwVsz_p6wsqTOapGt5CllIFgU=".decodeBase64()!!,
      ),
    )
    assertThat(WasmoJson.decodeFromString<PasskeyAuthentication>(json)).isEqualTo(value)
  }
}
