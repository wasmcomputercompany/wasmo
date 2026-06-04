package com.wasmo.support.wit

import assertk.assertThat
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
  fun `multiple packages`() {
    val wit = """
      |package wasi:clocks@0.2.9;
      |package wasi:clocks;
      """.trimMargin()
    val witReader = WitReader(wit)
    val e = assertFailsWith<WitException> {
      witReader.read()
    }
    assertThat(e.issue).isEqualTo("unexpected package identifier")
  }

  @Test
  fun `package after another declaration`() {
    val wit = """
      |interface foo {}
      |package wasi:clocks;
      """.trimMargin()
    val witReader = WitReader(wit)
    val e = assertFailsWith<WitException> {
      witReader.read()
    }
    assertThat(e.issue).isEqualTo("unexpected package identifier")
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
    assertThat(e.issue).isEqualTo("unexpected field: unstable.version")
  }

  @Test
  fun `readGate repeated unstable`() {
    val e = assertFailsWith<WitException> {
      WitReader("@unstable(feature = fancier-foo) @unstable(feature = faster-foo)").readGateOrNull()
    }
    assertThat(e.issue).isEqualTo("unexpected field: unstable.feature")
  }

  @Test
  fun `readGate repeated since`() {
    val e = assertFailsWith<WitException> {
      WitReader("@since(version = 0.2.0) @since(version = 0.3.0)").readGateOrNull()
    }
    assertThat(e.issue).isEqualTo("unexpected field: since.version")
  }

  @Test
  fun `readGate repeated deprecated`() {
    val e = assertFailsWith<WitException> {
      WitReader("@deprecated(version = 0.2.0) @deprecated(version = 0.3.0)").readGateOrNull()
    }
    assertThat(e.issue).isEqualTo("unexpected field: deprecated.version")
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
            name = Identifier("foo"),
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
            name = Identifier("foo"),
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
            name = Identifier("foo"),
            declarations = listOf(
              Function(
                location = Location(2, 3),
                name = Identifier("print"),
                parameters = listOf(
                  Parameter(
                    Location(2, 15),
                    "message",
                    TypeName.String,
                  ),
                  Parameter(
                    Location(2, 32),
                    Identifier("repeat"),
                    TypeName.Option(TypeName.U32),
                  ),
                ),
                returnType = TypeName.Result(
                  err = TypeName.Declared("errno"),
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
            name = Identifier("wall-clock"),
            declarations = listOf(
              Record(
                location = Location(4, 3),
                name = Identifier("datetime"),
                fields = listOf(
                  Field(
                    location = Location(5, 5),
                    name = Identifier("seconds"),
                    type = TypeName.U64,
                  ),
                  Field(
                    location = Location(6, 5),
                    name = Identifier("nanoseconds"),
                    type = TypeName.U32,
                  ),
                ),
              ),
              Function(
                location = Location(9, 3),
                name = Identifier("now"),
                parameters = listOf(),
                returnType = TypeName.Declared("datetime"),
              ),
              Function(
                location = Location(11, 3),
                name = Identifier("resolution"),
                parameters = listOf(),
                returnType = TypeName.Declared("datetime"),
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
            name = Identifier("wall-clock"),
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
            name = Identifier("wall-clock"),
            declarations = listOf(
              Record(
                documentation = Documentation(" spacetime"),
                gate = Gate(since = "2.0"),
                location = Location(4, 3),
                name = Identifier("datetime"),
                fields = listOf(
                  Field(
                    documentation = Documentation(" just a second"),
                    gate = Gate(since = "3.0"),
                    location = Location(7, 5),
                    name = Identifier("seconds"),
                    type = TypeName.U64,
                  ),
                  Field(
                    documentation = Documentation(" tick"),
                    gate = Gate(since = "4.0"),
                    location = Location(10, 5),
                    name = Identifier("nanoseconds"),
                    type = TypeName.U32,
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
            name = Identifier("wall-clock"),
            declarations = listOf(
              Function(
                documentation = Documentation(" sample the clock"),
                gate = Gate(since = "5.0"),
                location = Location(4, 3),
                name = Identifier("now"),
                parameters = listOf(),
                returnType = TypeName.Declared("datetime"),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `function parameters trailing commas`() {
    val wit = """
      |interface monotonic-clock {
      |  subscribe-instant: func(
      |    when: instant,
      |  ) -> pollable;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = Identifier("monotonic-clock"),
            declarations = listOf(
              Function(
                location = Location(2, 3),
                name = Identifier("subscribe-instant"),
                parameters = listOf(
                  Parameter(
                    location = Location(3, 5),
                    name = "when",
                    type = TypeName.Declared("instant"),
                  ),
                ),
                returnType = TypeName.Declared("pollable"),
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
            name = Identifier("db"),
            declarations = listOf(
              Resource(
                documentation = Documentation(" big boi"),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                name = Identifier("blob"),
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
                        type = TypeName.List(TypeName.U8),
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
                        type = TypeName.List(TypeName.U8),
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
                        type = TypeName.U32,
                      ),
                    ),
                    returnType = TypeName.List(TypeName.U8),
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
                        type = TypeName.Borrow(TypeName.Declared("blob")),
                      ),
                      Parameter(
                        location = Location(19, 43),
                        name = Identifier("rhs"),
                        type = TypeName.Borrow(TypeName.Declared("blob")),
                      ),
                    ),
                    returnType = TypeName.Declared("blob"),
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
            name = Identifier("db"),
            declarations = listOf(
              Resource(
                location = Location(2, 3),
                name = Identifier("blob"),
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
            name = Identifier("db"),
            declarations = listOf(
              Variant(
                documentation = Documentation(" whats included"),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                name = Identifier("filter"),
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
                    type = TypeName.List(TypeName.String),
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
  fun `flags documentation and gates`() {
    val wit = """
      |interface db {
      |  /// comic character
      |  @since(version = 1.0)
      |  flags properties {
      |    /// plastic
      |    @since(version = 2.0)
      |    lego,
      |    /// avenger
      |    @since(version = 3.0)
      |    marvel-superhero,
      |    /// naughty
      |    @since(version = 4.0)
      |    supervillain,
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = Identifier("db"),
            declarations = listOf(
              Flags(
                documentation = Documentation(" comic character"),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                name = Identifier("properties"),
                flags = listOf(
                  Flag(
                    documentation = Documentation(" plastic"),
                    gate = Gate(since = "2.0"),
                    location = Location(7, 5),
                    name = Identifier("lego"),
                  ),
                  Flag(
                    documentation = Documentation(" avenger"),
                    gate = Gate(since = "3.0"),
                    location = Location(10, 5),
                    name = Identifier("marvel-superhero"),
                  ),
                  Flag(
                    documentation = Documentation(" naughty"),
                    gate = Gate(since = "4.0"),
                    location = Location(13, 5),
                    name = Identifier("supervillain"),
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
  fun `enum documentation and gates`() {
    val wit = """
      |interface db {
      |  /// Roy G.
      |  @since(version = 1.0)
      |  enum color {
      |    /// #ff0000
      |    @since(version = 2.0)
      |    red,
      |    /// #0000ff
      |    @since(version = 3.0)
      |    blue,
      |    /// #00ff00
      |    @since(version = 4.0)
      |    green,
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = Identifier("db"),
            declarations = listOf(
              Enum(
                documentation = Documentation(" Roy G."),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                name = Identifier("color"),
                cases = listOf(
                  Case(
                    documentation = Documentation(" #ff0000"),
                    gate = Gate(since = "2.0"),
                    location = Location(7, 5),
                    name = Identifier("red"),
                  ),
                  Case(
                    documentation = Documentation(" #0000ff"),
                    gate = Gate(since = "3.0"),
                    location = Location(10, 5),
                    name = Identifier("blue"),
                  ),
                  Case(
                    documentation = Documentation(" #00ff00"),
                    gate = Gate(since = "4.0"),
                    location = Location(13, 5),
                    name = Identifier("green"),
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
  fun `type alias documentation and gates`() {
    val wit = """
      |interface db {
      |  /// So Awesome.
      |  @since(version = 1.0)
      |  type my-awesome-u32 = u32;
      |  /// So Complicated.
      |  @since(version = 2.0)
      |  type my-complicated-tuple = tuple<u32, s32, string>;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = Identifier("db"),
            declarations = listOf(
              TypeAlias(
                documentation = Documentation(" So Awesome."),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                name = Identifier("my-awesome-u32"),
                target = TypeName.U32,
              ),
              TypeAlias(
                documentation = Documentation(" So Complicated."),
                gate = Gate(since = "2.0"),
                location = Location(7, 3),
                name = Identifier("my-complicated-tuple"),
                target = TypeName.Tuple(
                  listOf(
                    TypeName.U32,
                    TypeName.S32,
                    TypeName.String,
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
  fun `use documentation and gates`() {
    val wit = """
      |interface db {
      |  /// Four values.
      |  @since(version = 1.0)
      |  use an-interface.{a, list, of, names};
      |  /// One aliased value.
      |  @since(version = 2.0)
      |  use my:dependency/the-interface@3.0.{more, names as foo};
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Interface(
            location = Location(1, 1),
            name = Identifier("db"),
            declarations = listOf(
              Use(
                documentation = Documentation(" Four values."),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                path = UsePath(name = Identifier("an-interface")),
                items = listOf(
                  Use.Item(name = Identifier("a")),
                  Use.Item(name = Identifier("list")),
                  Use.Item(name = Identifier("of")),
                  Use.Item(name = Identifier("names")),
                ),
              ),
              Use(
                documentation = Documentation(" One aliased value."),
                gate = Gate(since = "2.0"),
                location = Location(7, 3),
                path = UsePath(
                  namespaces = listOf(Identifier("my")),
                  packageNames = listOf(Identifier("dependency")),
                  name = Identifier("the-interface"),
                  version = SemVer("3.0"),
                ),
                items = listOf(
                  Use.Item(name = Identifier("more")),
                  Use.Item(name = Identifier("names"), alias = Identifier("foo")),
                ),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `world documentation and gates`() {
    val wit = """
      |/// a printer-scanner-fax thingy
      |@since(version = 1.0)
      |world multi-function-device {
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            documentation = Documentation(" a printer-scanner-fax thingy"),
            gate = Gate(since = "1.0"),
            location = Location(3, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(),
          ),
        ),
      ),
    )
  }

  @Test
  fun `import export use path documentation and gates`() {
    val wit = """
      |world multi-function-device {
      |  /// The component needs an `error-reporter`
      |  @since(version = 1.0)
      |  import error-reporter;
      |  /// This also exports an `error-creator`
      |  @since(version = 2.0)
      |  export error-creator;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Import(
                documentation = Documentation(" The component needs an `error-reporter`"),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                value = ExternalUsePath(
                  path = UsePath(name = Identifier("error-reporter")),
                ),
              ),
              Export(
                documentation = Documentation(" This also exports an `error-creator`"),
                gate = Gate(since = "2.0"),
                location = Location(7, 3),
                value = ExternalUsePath(
                  path = UsePath(name = Identifier("error-creator")),
                ),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `import plain named use path documentation and gates`() {
    val wit = """
      |world multi-function-device {
      |  /// This store is aliased as 'primary'
      |  @since(version = 1.0)
      |  import primary: wasi:keyvalue/store;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Import(
                documentation = Documentation(" This store is aliased as 'primary'"),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                value = ExternalUsePath(
                  plainName = Identifier("primary"),
                  path = UsePath(
                    namespaces = listOf(Identifier("wasi")),
                    packageNames = listOf(Identifier("keyvalue")),
                    name = Identifier("store"),
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
  fun `export plain named use path documentation and gates`() {
    val wit = """
      |world multi-function-device {
      |  /// This store is aliased as 'secondary'
      |  @since(version = 2.0)
      |  export secondary: wasi:keyvalue/store;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Export(
                documentation = Documentation(" This store is aliased as 'secondary'"),
                gate = Gate(since = "2.0"),
                location = Location(4, 3),
                value = ExternalUsePath(
                  plainName = Identifier("secondary"),
                  path = UsePath(
                    namespaces = listOf(Identifier("wasi")),
                    packageNames = listOf(Identifier("keyvalue")),
                    name = Identifier("store"),
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
  fun `import inline interface documentation and gates`() {
    val wit = """
      |world multi-function-device {
      |  /// This interface is inline
      |  @since(version = 1.0)
      |  import host: interface {
      |    /// This function is in an inline interface
      |    @since(version = 2.0)
      |    log: func(param: string);
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Import(
                location = Location(4, 3),
                value = Interface(
                  documentation = Documentation(" This interface is inline"),
                  gate = Gate(since = "1.0"),
                  location = Location(4, 3),
                  name = Identifier("host"),
                  declarations = listOf(
                    Function(
                      documentation = Documentation(" This function is in an inline interface"),
                      gate = Gate(since = "2.0"),
                      location = Location(7, 5),
                      name = Identifier("log"),
                      parameters = listOf(
                        Parameter(
                          location = Location(7, 15),
                          name = Identifier("param"),
                          type = TypeName.String,
                        ),
                      ),
                    ),
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
  fun `export inline interface documentation and gates`() {
    val wit = """
      |world multi-function-device {
      |  /// We can export an inline interface
      |  @since(version = 3.0)
      |  export guest: interface {
      |    /// A function in an inline interface
      |    @since(version = 4.0)
      |    scan: func(document: string);
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Export(
                location = Location(4, 3),
                value = Interface(
                  documentation = Documentation(" We can export an inline interface"),
                  gate = Gate(since = "3.0"),
                  location = Location(4, 3),
                  name = Identifier("guest"),
                  declarations = listOf(
                    Function(
                      documentation = Documentation(" A function in an inline interface"),
                      gate = Gate(since = "4.0"),
                      location = Location(7, 5),
                      name = Identifier("scan"),
                      parameters = listOf(
                        Parameter(
                          location = Location(7, 16),
                          name = Identifier("document"),
                          type = TypeName.String,
                        ),
                      ),
                    ),
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
  fun `import inline function documentation and gates`() {
    val wit = """
      |world multi-function-device {
      |  /// This function is inline
      |  @since(version = 4.0)
      |  import log: func(param: string);
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Import(
                location = Location(4, 3),
                value = Function(
                  documentation = Documentation(" This function is inline"),
                  gate = Gate(since = "4.0"),
                  location = Location(4, 3),
                  name = Identifier("log"),
                  parameters = listOf(
                    Parameter(
                      location = Location(4, 20),
                      name = Identifier("param"),
                      type = TypeName.String,
                    ),
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
  fun `export inline function documentation and gates`() {
    val wit = """
      |world multi-function-device {
      |  /// This exported function is inline
      |  @since(version = 1.0)
      |  export scan: func(document: string);
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Export(
                location = Location(4, 3),
                value = Function(
                  documentation = Documentation(" This exported function is inline"),
                  gate = Gate(since = "1.0"),
                  location = Location(4, 3),
                  name = Identifier("scan"),
                  parameters = listOf(
                    Parameter(
                      location = Location(4, 21),
                      name = Identifier("document"),
                      type = TypeName.String,
                    ),
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
  fun `import plain named simple use path`() {
    val wit = """
      |world multi-function-device {
      |  import two: store;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Import(
                location = Location(2, 3),
                value = ExternalUsePath(
                  plainName = Identifier("two"),
                  path = UsePath(name = Identifier("store")),
                ),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `export plain named simple use path`() {
    val wit = """
      |world multi-function-device {
      |  export two: store;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Export(
                location = Location(2, 3),
                value = ExternalUsePath(
                  plainName = Identifier("two"),
                  path = UsePath(name = Identifier("store")),
                ),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `world include documentation and gates`() {
    val wit = """
      |world multi-function-device {
      |  /// This include is pretty basic.
      |  @since(version = 1.0)
      |  include my-world-2;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Include(
                documentation = Documentation(" This include is pretty basic."),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                path = UsePath(name = Identifier("my-world-2")),
                items = listOf(),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `world include with items`() {
    val wit = """
      |world multi-function-device {
      |  include wasi:io/my-world-1 with { a as a1, b as b1 };
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Include(
                location = Location(2, 3),
                path = UsePath(
                  namespaces = listOf(Identifier("wasi")),
                  packageNames = listOf(Identifier("io")),
                  name = Identifier("my-world-1"),
                ),
                items = listOf(
                  Include.Item(
                    name = Identifier("a"),
                    alias = Identifier("a1"),
                  ),
                  Include.Item(
                    name = Identifier("b"),
                    alias = Identifier("b1"),
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
  fun `inline package documentation and gates`() {
    val wit = """
      |/// This package is pasted from somewhere else.
      |@since(version = 1.0)
      |package local:a {
      |  /// This interface is included in a package.
      |  @since(version = 2.0)
      |  interface foo {}
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Package(
            documentation = Documentation(" This package is pasted from somewhere else."),
            gate = Gate(since = "1.0"),
            location = Location(3, 1),
            name = PackageName("local", "a"),
            declarations = listOf(
              Interface(
                documentation = Documentation(" This interface is included in a package."),
                gate = Gate(since = "2.0"),
                location = Location(6, 3),
                name = Identifier("foo"),
                declarations = listOf(),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `top level use documentation and gates`() {
    val wit = """
      |/// Use the Wasi HTTP types.
      |@since(version = 1.0)
      |use wasi:http/types@1.0.0;
      |/// Use the Wasi HTTP handler also.
      |@since(version = 2.0)
      |use wasi:http/handler as http-handler;
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          TopLevelUse(
            documentation = Documentation(" Use the Wasi HTTP types."),
            gate = Gate(since = "1.0"),
            location = Location(3, 1),
            path = UsePath(
              namespaces = listOf(Identifier("wasi")),
              packageNames = listOf(Identifier("http")),
              name = Identifier("types"),
              version = SemVer("1.0.0"),
            ),
          ),
          TopLevelUse(
            documentation = Documentation(" Use the Wasi HTTP handler also."),
            gate = Gate(since = "2.0"),
            location = Location(6, 1),
            path = UsePath(
              namespaces = listOf(Identifier("wasi")),
              packageNames = listOf(Identifier("http")),
              name = Identifier("handler"),
            ),
            alias = Identifier("http-handler"),
          ),
        ),
      ),
    )
  }

  @Test
  fun `top level use in nested package`() {
    val wit = """
      |package local:a {
      |  /// Use the Wasi HTTP types.
      |  @since(version = 1.0)
      |  use wasi:http/types@1.0.0;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Package(
            location = Location(1, 1),
            name = PackageName("local", "a"),
            declarations = listOf(
              TopLevelUse(
                documentation = Documentation(" Use the Wasi HTTP types."),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                path = UsePath(
                  namespaces = listOf(Identifier("wasi")),
                  packageNames = listOf(Identifier("http")),
                  name = Identifier("types"),
                  version = SemVer("1.0.0"),
                ),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `world in nested package`() {
    val wit = """
      |package local:a {
      |  /// a printer-scanner-fax thingy
      |  @since(version = 1.0)
      |  world multi-function-device {
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          Package(
            location = Location(1, 1),
            name = PackageName("local", "a"),
            declarations = listOf(
              World(
                documentation = Documentation(" a printer-scanner-fax thingy"),
                gate = Gate(since = "1.0"),
                location = Location(4, 3),
                name = Identifier("multi-function-device"),
                declarations = listOf(),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `record in world`() {
    val wit = """
      |world multi-function-device {
      |  record datetime {
      |    seconds: u64,
      |    nanoseconds: u32,
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Record(
                location = Location(2, 3),
                name = Identifier("datetime"),
                fields = listOf(
                  Field(
                    location = Location(3, 5),
                    name = Identifier("seconds"),
                    type = TypeName.U64,
                  ),
                  Field(
                    location = Location(4, 5),
                    name = Identifier("nanoseconds"),
                    type = TypeName.U32,
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
  fun `enum in world`() {
    val wit = """
      |world multi-function-device {
      |  enum color {
      |    red,
      |    blue,
      |    green,
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Enum(
                location = Location(2, 3),
                name = Identifier("color"),
                cases = listOf(
                  Case(
                    location = Location(3, 5),
                    name = Identifier("red"),
                  ),
                  Case(
                    location = Location(4, 5),
                    name = Identifier("blue"),
                  ),
                  Case(
                    location = Location(5, 5),
                    name = Identifier("green"),
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
  fun `flags in world`() {
    val wit = """
      |world multi-function-device {
      |  flags properties {
      |    lego,
      |    marvel-superhero,
      |    supervillain,
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Flags(
                location = Location(2, 3),
                name = Identifier("properties"),
                flags = listOf(
                  Flag(
                    location = Location(3, 5),
                    name = Identifier("lego"),
                  ),
                  Flag(
                    location = Location(4, 5),
                    name = Identifier("marvel-superhero"),
                  ),
                  Flag(
                    location = Location(5, 5),
                    name = Identifier("supervillain"),
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
  fun `resource in world`() {
    val wit = """
      |world multi-function-device {
      |  resource blob {
      |    constructor(init: list<u8>);
      |    write: func(bytes: list<u8>);
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Resource(
                location = Location(2, 3),
                name = Identifier("blob"),
                declarations = listOf(
                  Function(
                    location = Location(3, 5),
                    constructor = true,
                    name = Identifier("constructor"),
                    parameters = listOf(
                      Parameter(
                        location = Location(3, 17),
                        name = Identifier("init"),
                        type = TypeName.List(TypeName.U8),
                      ),
                    ),
                    returnType = null,
                  ),
                  Function(
                    location = Location(4, 5),
                    name = Identifier("write"),
                    parameters = listOf(
                      Parameter(
                        location = Location(4, 17),
                        name = Identifier("bytes"),
                        type = TypeName.List(TypeName.U8),
                      ),
                    ),
                    returnType = null,
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
  fun `type alias in world`() {
    val wit = """
      |world multi-function-device {
      |  type my-awesome-u32 = u32;
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              TypeAlias(
                location = Location(2, 3),
                name = Identifier("my-awesome-u32"),
                target = TypeName.U32,
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `use in world`() {
    val wit = """
      |world multi-function-device {
      |  use an-interface.{a, list, of, names};
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Use(
                location = Location(2, 3),
                path = UsePath(name = Identifier("an-interface")),
                items = listOf(
                  Use.Item(name = Identifier("a")),
                  Use.Item(name = Identifier("list")),
                  Use.Item(name = Identifier("of")),
                  Use.Item(name = Identifier("names")),
                ),
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `variant in world`() {
    val wit = """
      |world multi-function-device {
      |  variant filter {
      |    all,
      |    none,
      |    some(list<string>),
      |  }
      |}
      """.trimMargin()
    val witReader = WitReader(wit)
    assertThat(witReader.read()).isEqualTo(
      WitFile(
        declarations = listOf(
          World(
            location = Location(1, 1),
            name = Identifier("multi-function-device"),
            declarations = listOf(
              Variant(
                location = Location(2, 3),
                name = Identifier("filter"),
                cases = listOf(
                  Case(
                    location = Location(3, 5),
                    name = Identifier("all"),
                  ),
                  Case(
                    location = Location(4, 5),
                    name = Identifier("none"),
                  ),
                  Case(
                    location = Location(5, 5),
                    name = Identifier("some"),
                    type = TypeName.List(TypeName.String),
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
