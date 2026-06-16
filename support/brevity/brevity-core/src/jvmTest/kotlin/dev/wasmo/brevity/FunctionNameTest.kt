package dev.wasmo.brevity

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class FunctionNameTest {
  @Test
  fun happyPath() {
    val functionHas = FunctionName(
      packageName = "wasi:http@0.3.0",
      parentName = "types",
      name = "has",
    )
    assertThat(functionHas.abiName).isEqualTo("has")
    assertThat(functionHas.moduleName).isEqualTo("wasi:http/types@0.3.0")

    val functionFields = FunctionName(
      packageName = "wasi:http@0.3.0",
      parentName = "types",
      name = "fields",
      annotation = Annotation.Constructor,
    )
    assertThat(functionFields.abiName).isEqualTo("[constructor]fields")
    assertThat(functionFields.moduleName).isEqualTo("wasi:http/types@0.3.0")

    val functionFromList = FunctionName(
      packageName = "wasi:http@0.3.0",
      parentName = "types",
      name = "from-list",
      resourceName = "fields",
      annotation = Annotation.Static,
    )
    assertThat(functionFromList.abiName).isEqualTo("[static]fields.from-list")
    assertThat(functionFromList.moduleName).isEqualTo("wasi:http/types@0.3.0")

    val functionResourceHas = FunctionName(
      packageName = "wasi:http@0.3.0",
      parentName = "types",
      name = "has",
      resourceName = "fields",
      annotation = Annotation.Method,
    )
    assertThat(functionResourceHas.abiName).isEqualTo("[method]fields.has")
    assertThat(functionResourceHas.moduleName).isEqualTo("wasi:http/types@0.3.0")
  }
}
