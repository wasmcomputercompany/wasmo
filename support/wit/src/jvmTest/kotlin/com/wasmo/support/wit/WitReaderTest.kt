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
    assertThat(gate).isEqualTo(Gate(unstable = "fancier-foo"))
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
  fun `readInterface success`() {
    val wit = """
      |interface foo {}
      """.trimMargin()
    assertThat(WitReader(wit).read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = TypeName("foo"),
            declarations = listOf(),
          ),
        ),
      ),
    )
  }

  @Test
  fun `readInterface with documentation and gates`() {
    val wit = """
      |/// this is the foo interface
      |@deprecated(version = 0.2.2)
      |/**it is a good interface*/
      |interface foo {}
      """.trimMargin()
    assertThat(WitReader(wit).read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            documentation = Documentation(
              """
              | this is the foo interface
              |it is a good interface
              """.trimMargin(),
            ),
            gate = Gate(deprecated = "0.2.2"),
            location = Location(4, 1),
            name = TypeName("foo"),
            declarations = listOf(),
          ),
        ),
      ),
    )
  }

  @Test
  fun `readInterface with functions`() {
    val wit = """
      |interface foo {
      |  print: func(message: string, repeat: option<u32>) -> result<_, errno>;
      |  async-print: async func();
      |}
      """.trimMargin()
    assertThat(WitReader(wit).read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = TypeName("foo"),
            declarations = listOf(
              Function(
                location = Location(2, 3),
                name = Identifier("print"),
                parameters = listOf(
                  Parameter(
                    Location(2, 15),
                    "message",
                    "string",
                  ),
                  Parameter(
                    Location(2, 32),
                    Identifier("repeat"),
                    TypeName.Option(TypeName("u32")),
                  ),
                ),
                returnType = TypeName.Result(
                  err = TypeName("errno"),
                ),
              ),
              Function(
                location = Location(3, 3),
                name = Identifier("async-print"),
                async = true,
                parameters = listOf(),
              ),
            ),
          ),
        ),
      ),
    )
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
