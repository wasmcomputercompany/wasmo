package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import okio.Path.Companion.toPath

class TypeReferenceTest {
  @Test
  fun `collect type references`() {
    val path = "clock.wit".toPath()
    val witFile = """
      |package wasi:clocks@0.2.12;
      |interface monotonic-clock {
      |    use wasi:io/poll@0.2.12.{pollable};
      |    type instant = u64;
      |    now: func() -> instant;
      |    subscribe-instant: func(when: instant) -> pollable;
      |}
      """.trimMargin().toWitFile()
    val witPackage = WitPackage(
      packageName = "wasi:clocks@0.2.12".toPackageName(),
      files = mapOf(path to witFile),
    )

    assertThat(witPackage.typeReferences()).containsExactly(
      TypeReference(
        path = path,
        offset = Offset(3, 5),
        packageName = "wasi:io@0.2.12".toPackageName(),
        interfaceName = Identifier("poll"),
        typeName = TypeName.Declared("pollable"),
      ),
      TypeReference(
        path = path,
        offset = Offset(4, 5),
        packageName = "wasi:clocks@0.2.12".toPackageName(),
        interfaceName = Identifier("monotonic-clock"),
        typeName = TypeName.U64,
      ),
      TypeReference(
        path = path,
        offset = Offset(5, 5),
        packageName = "wasi:clocks@0.2.12".toPackageName(),
        interfaceName = Identifier("monotonic-clock"),
        typeName = TypeName.Declared("instant"),
      ),
      TypeReference(
        path = path,
        offset = Offset(6, 5),
        packageName = "wasi:clocks@0.2.12".toPackageName(),
        interfaceName = Identifier("monotonic-clock"),
        typeName = TypeName.Declared("instant"),
      ),
      TypeReference(
        path = path,
        offset = Offset(6, 5),
        packageName = "wasi:clocks@0.2.12".toPackageName(),
        interfaceName = Identifier("monotonic-clock"),
        typeName = TypeName.Declared("pollable"),
      ),
    )
  }
}
