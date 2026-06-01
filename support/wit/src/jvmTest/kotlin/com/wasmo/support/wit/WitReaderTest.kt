package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Ignore
import kotlin.test.Test

class WitReaderTest {
  @Test
  @Ignore("not implemented")
  fun happyPath() {
    val wit = """
      |package wasi:clocks@0.2.9;
      |
      |interface wall-clock {
      |  record datetime {
      |    seconds: u64,
      |    nanoseconds: u32,
      |  }
      |
      |  now: func() -> datetime;
      |
      |  resolution: func() -> datetime;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        packageName = PackageName("wasi", "clocks", "0.2.9"),
        declarations = listOf(
          Interface(
            location = Location(3, 1),
            name = TypeName("wall-clock"),
            declarations = listOf(
              Record(
                location = Location(4, 3),
                name = TypeName("datetime"),
                fields = listOf(
                  Field(
                    location = Location(5, 5),
                    name = Identifier("seconds"),
                    typeName = Types.u64,
                  ),
                  Field(
                    location = Location(6, 5),
                    name = Identifier("nanoseconds"),
                    typeName = Types.u32,
                  ),
                ),
              ),
              Function(
                location = Location(9, 3),
                name = Identifier("now"),
                parameters = listOf(),
                returnType = TypeName("datetime"),
              ),
              Function(
                location = Location(9, 3),
                name = Identifier("resolution"),
                parameters = listOf(),
                returnType = TypeName("datetime"),
              ),
            ),
          ),
        ),
      ),
    )
  }
}
