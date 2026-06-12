package dev.wasmo.brevity

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class FunctionNameTest {
  @Test
  fun abiName() {
    assertThat(
      FunctionName(
        packageName = "wasi:http@0.3.0",
        name = "has",
      ).abiName,
    ).isEqualTo("has")

    assertThat(
      FunctionName(
        packageName = "wasi:http@0.3.0",
        name = "fields",
        annotation = Annotation.Constructor,
      ).abiName,
    ).isEqualTo("[constructor]fields")

    assertThat(
      FunctionName(
        packageName = "wasi:http@0.3.0",
        name = "from-list",
        resourceName = "fields",
        annotation = Annotation.Static,
      ).abiName,
    ).isEqualTo("[static]fields.from-list")

    assertThat(
      FunctionName(
        packageName = "wasi:http@0.3.0",
        name = "has",
        resourceName = "fields",
        annotation = Annotation.Method,
      ).abiName,
    ).isEqualTo("[method]fields.has")
  }
}
