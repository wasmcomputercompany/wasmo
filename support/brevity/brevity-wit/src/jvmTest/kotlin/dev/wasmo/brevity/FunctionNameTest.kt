package dev.wasmo.brevity

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class FunctionNameTest {
  @Test
  fun `function on world`() {
    val function = FunctionName(
      name = "sum",
    )
    assertThat(function.moduleName).isNull()
    assertThat(function.abiName).isEqualTo("sum")
  }

  @Test
  fun `function on interface`() {
    val function = FunctionName(
      packageName = "wasi:http@0.3.0",
      parentName = "types",
      name = "has",
    )
    assertThat(function.moduleName).isEqualTo("wasi:http/types@0.3.0")
    assertThat(function.abiName).isEqualTo("has")
  }

  @Test
  fun `package name with sem ver`() {
    val function = FunctionName(
      packageName = "wasi:http@0.3.0",
      parentName = "types",
      name = "fields",
      annotation = Annotation.Constructor,
    )
    assertThat(function.moduleName).isEqualTo("wasi:http/types@0.3.0")
    assertThat(function.abiName).isEqualTo("[constructor]fields")
  }

  @Test
  fun `function on resource`() {
    val function = FunctionName(
      packageName = "wasi:http@0.3.0",
      parentName = "types",
      name = "from-list",
      resourceName = "fields",
      annotation = Annotation.Static,
    )
    assertThat(function.moduleName).isEqualTo("wasi:http/types@0.3.0")
    assertThat(function.abiName).isEqualTo("[static]fields.from-list")
  }

  @Test
  fun `static function`() {
    val function = FunctionName(
      packageName = "wasi:http@0.3.0",
      parentName = "types",
      name = "has",
      resourceName = "fields",
      annotation = Annotation.Method,
    )
    assertThat(function.moduleName).isEqualTo("wasi:http/types@0.3.0")
    assertThat(function.abiName).isEqualTo("[method]fields.has")
  }
}
