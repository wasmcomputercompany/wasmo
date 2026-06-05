package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

class TypeReferenceTest {
  @Test
  fun `collect type references`() {
    val witFile = WitReader(
      """
      |package wasi:clocks@0.2.12;
      |interface monotonic-clock {
      |    use wasi:io/poll@0.2.12.{pollable};
      |    type instant = u64;
      |    now: func() -> instant;
      |    subscribe-instant: func(when: instant) -> pollable;
      |}
      """.trimMargin(),
    ).read()

    assertThat(witFile.typeReferences()).containsExactly(
      TypeReference(
        location = Location(3, 5),
        packageName = PackageName("wasi", "io", "0.2.12"),
        interfaceName = Identifier("poll"),
        typeName = TypeName.Declared("pollable"),
      ),
      TypeReference(
        location = Location(4, 5),
        packageName = PackageName("wasi", "clocks", "0.2.12"),
        interfaceName = Identifier("monotonic-clock"),
        typeName = TypeName.U64,
      ),
      TypeReference(
        location = Location(5, 5),
        packageName = PackageName("wasi", "clocks", "0.2.12"),
        interfaceName = Identifier("monotonic-clock"),
        typeName = TypeName.Declared("instant"),
      ),
      TypeReference(
        location = Location(6, 5),
        packageName = PackageName("wasi", "clocks", "0.2.12"),
        interfaceName = Identifier("monotonic-clock"),
        typeName = TypeName.Declared("instant"),
      ),
      TypeReference(
        location = Location(6, 5),
        packageName = PackageName("wasi", "clocks", "0.2.12"),
        interfaceName = Identifier("monotonic-clock"),
        typeName = TypeName.Declared("pollable"),
      ),
    )
  }
}
