package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFailsWith

class WitReaderTest {
  @Test
  fun packageOnly() {
    val wit = """
      |package wasi:clocks@0.2.9;
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        packageName = PackageName("wasi", "clocks", "0.2.9"),
        declarations = listOf(),
      ),
    )
  }

  @Test
  fun `readGate success`() {
    val wit = """
      |@since(version = 0.2.0)
      |@deprecated(version = 0.2.2)
      |@unstable(feature = fancier-foo)
      |interface foo {}
      """.trimMargin()
    val gate = WitReader(wit).readGateOrNull()
    assertThat(gate).isEqualTo(
      Gate(
        unstable = "fancier-foo",
        since = "0.2.0",
        deprecated = "0.2.2",
      ),
    )
  }

  @Test
  fun `readGate unstable only`() {
    val wit = """
      |@unstable(feature = fancier-foo)
      |interface foo {}
      """.trimMargin()
    val gate = WitReader(wit).readGateOrNull()
    assertThat(gate).isEqualTo(Gate(unstable = "fancier-foo"),
    )
  }

  @Test
  fun `readGate since only`() {
    val wit = """
      |@since(version = 0.2.0)
      |interface foo {}
      """.trimMargin()
    val gate = WitReader(wit).readGateOrNull()
    assertThat(gate).isEqualTo(Gate(since = "0.2.0"))
  }

  @Test
  fun `readGate deprecated only`() {
    val wit = """
      |@deprecated(version = 0.2.2)
      |interface foo {}
      """.trimMargin()
    val gate = WitReader(wit).readGateOrNull()
    assertThat(gate).isEqualTo(Gate(deprecated = "0.2.2"))
  }

  @Test
  fun `readGate unexpected field`() {
    val e = assertFailsWith<WitException> {
      WitReader("@unstable(version = 0.2.2)").readGateOrNull()
    }
    assertThat(e).hasMessage("unexpected field: unstable.version")
  }

  @Test
  fun `readGate repeated gate item`() {
    val e = assertFailsWith<WitException> {
      WitReader("@since(version = 0.2.0) @since(version = 0.2.0)").readGateOrNull()
    }
    assertThat(e).hasMessage("unexpected field: since.version")
  }

  @Test
  fun `readGate absent`() {
    assertThat(WitReader("interface foo {}").readGateOrNull()).isNull()
  }

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
