package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
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
  fun `read sample interface`() {
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
                location = Location(11, 3),
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

  @Test
  fun `interface documentation and gates`() {
    val wit = """
      |/// tick tock
      |/// wall clock
      |@since(version = 1.0)
      |interface wall-clock {
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            documentation = Documentation(
              """
              | tick tock
              | wall clock
              """.trimMargin(),
            ),
            gate = Gate(since = "1.0"),
            location = Location(4, 1),
            name = TypeName("wall-clock"),
            declarations = listOf(),
          ),
        ),
      ),
    )
  }

  @Test
  fun `record documentation and gates`() {
    val wit = """
      |interface wall-clock {
      |  /// spacetime
      |  @since(version = 2.0)
      |  record datetime {
      |    /// just a second
      |    @since(version = 3.0)
      |    seconds: u64,
      |    /// tick
      |    @since(version = 4.0)
      |    nanoseconds: u32,
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = TypeName("wall-clock"),
            declarations = listOf(
              Record(
                documentation = Documentation(" spacetime"),
                gate = Gate(since = "2.0"),
                location = Location(4, 3),
                name = TypeName("datetime"),
                fields = listOf(
                  Field(
                    documentation = Documentation(" just a second"),
                    gate = Gate(since = "3.0"),
                    location = Location(7, 5),
                    name = Identifier("seconds"),
                    typeName = Types.u64,
                  ),
                  Field(
                    documentation = Documentation(" tick"),
                    gate = Gate(since = "4.0"),
                    location = Location(10, 5),
                    name = Identifier("nanoseconds"),
                    typeName = Types.u32,
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `function documentation and gates`() {
    val wit = """
      |interface wall-clock {
      |  /// sample the clock
      |  @since(version = 5.0)
      |  now: func() -> datetime;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = TypeName("wall-clock"),
            declarations = listOf(
              Function(
                documentation = Documentation(" sample the clock"),
                gate = Gate(since = "5.0"),
                location = Location(4, 3),
                name = Identifier("now"),
                parameters = listOf(),
                returnType = TypeName("datetime"),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `resource documentation and gates`() {
    val wit = """
      |interface db {
      |  /// big boi
      |  @since(version = 1.0)
      |  resource blob {
      |    /// makes a new one
      |    @since(version = 2.0)
      |    constructor(init: list<u8>);
      |
      |    /// puts some bytes
      |    @since(version = 3.0)
      |    write: func(bytes: list<u8>);
      |
      |    /// gets some bytes
      |    @since(version = 4.0)
      |    read: func(n: u32) -> list<u8>;
      |
      |    /// smashes some blobs together
      |    @since(version = 5.0)
      |    merge: static func(lhs: borrow<blob>, rhs: borrow<blob>) -> blob;
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = TypeName("db"),
            declarations = listOf(
              Resource(
                documentation = Documentation(" big boi"),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                name = TypeName("blob"),
                declarations = listOf(
                  Function(
                    documentation = Documentation(" makes a new one"),
                    gate = Gate(since = "2.0"),
                    location = Location(7, 5),
                    constructor = true,
                    name = Identifier("constructor"),
                    parameters = listOf(
                      Parameter(
                        location = Location(7, 17),
                        name = Identifier("init"),
                        typeName = TypeName.List(TypeName("u8")),
                      ),
                    ),
                    returnType = null,
                  ),
                  Function(
                    documentation = Documentation(" puts some bytes"),
                    gate = Gate(since = "3.0"),
                    location = Location(11, 5),
                    name = Identifier("write"),
                    parameters = listOf(
                      Parameter(
                        location = Location(11, 17),
                        name = Identifier("bytes"),
                        typeName = TypeName.List(TypeName("u8")),
                      ),
                    ),
                    returnType = null,
                  ),
                  Function(
                    documentation = Documentation(" gets some bytes"),
                    gate = Gate(since = "4.0"),
                    location = Location(15, 5),
                    name = Identifier("read"),
                    parameters = listOf(
                      Parameter(
                        location = Location(15, 16),
                        name = Identifier("n"),
                        typeName = TypeName("u32"),
                      ),
                    ),
                    returnType = TypeName.List(TypeName("u8")),
                  ),
                  Function(
                    documentation = Documentation(" smashes some blobs together"),
                    gate = Gate(since = "5.0"),
                    location = Location(19, 5),
                    static = true,
                    name = Identifier("merge"),
                    parameters = listOf(
                      Parameter(
                        location = Location(19, 24),
                        name = Identifier("lhs"),
                        typeName = TypeName.Borrow(TypeName("blob")),
                      ),
                      Parameter(
                        location = Location(19, 43),
                        name = Identifier("rhs"),
                        typeName = TypeName.Borrow(TypeName("blob")),
                      ),
                    ),
                    returnType = TypeName("blob"),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `empty resource`() {
    val wit = """
      |interface db {
      |  resource blob;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = TypeName("db"),
            declarations = listOf(
              Resource(
                location = Location(2, 3),
                name = TypeName("blob"),
                declarations = listOf(),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `variant documentation and gates`() {
    val wit = """
      |interface db {
      |  /// whats included
      |  @since(version = 1.0)
      |  variant filter {
      |    /// all the things
      |    @since(version = 2.0)
      |    all,
      |    /// zilch
      |    @since(version = 3.0)
      |    none,
      |    /// one
      |    @since(version = 4.0)
      |    some(list<string>),
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = TypeName("db"),
            declarations = listOf(
              Variant(
                documentation = Documentation(" whats included"),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                name = TypeName("filter"),
                cases = listOf(
                  Case(
                    documentation = Documentation(" all the things"),
                    gate = Gate(since = "2.0"),
                    location = Location(7, 5),
                    name = Identifier("all"),
                  ),
                  Case(
                    documentation = Documentation(" zilch"),
                    gate = Gate(since = "3.0"),
                    location = Location(10, 5),
                    name = Identifier("none"),
                  ),
                  Case(
                    documentation = Documentation(" one"),
                    gate = Gate(since = "4.0"),
                    location = Location(13, 5),
                    name = Identifier("some"),
                    typeName = TypeName.List(TypeName("string"))
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    )
  }
}
