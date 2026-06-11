package com.wasmo.support.wit.io

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.wasmo.support.wit.Documentation
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.Offset
import com.wasmo.support.wit.WitException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class WitFileReaderTest {
  @Test
  fun packageOnly() {
    val wit = """
      |package wasi:clocks@0.2.9;
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        packageName = "wasi:clocks@0.2.9".toPackageName(),
      ),
    )
  }

  @Test
  fun `multiple packages`() {
    val e = assertFailsWith<WitException> {
      """
      |package wasi:clocks@0.2.9;
      |package wasi:clocks;
      """.trimMargin().toWitFile()
    }
    assertThat(e.issue).isEqualTo("unexpected package identifier")
  }

  @Test
  fun `package after another declaration`() {
    val e = assertFailsWith<WitException> {
      """
      |interface foo {}
      |package wasi:clocks;
      """.trimMargin().toWitFile()
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
    val gate = WitFileReader(wit).readGateOrNull()
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
    val gate = WitFileReader(wit).readGateOrNull()
    assertThat(gate).isEqualTo(Gate(unstable = "fancier-foo"))
  }

  @Test
  fun `readGate since only`() {
    val wit = """
      |@since(version = 0.2.0)
      |interface foo {}
      """.trimMargin()
    val gate = WitFileReader(wit).readGateOrNull()
    assertThat(gate).isEqualTo(Gate(since = "0.2.0"))
  }

  @Test
  fun `readGate deprecated only`() {
    val wit = """
      |@deprecated(version = 0.2.2)
      |interface foo {}
      """.trimMargin()
    val gate = WitFileReader(wit).readGateOrNull()
    assertThat(gate).isEqualTo(Gate(deprecated = "0.2.2"))
  }

  @Test
  fun `readGate unexpected field`() {
    val e = assertFailsWith<WitException> {
      WitFileReader("@unstable(version = 0.2.2)").readGateOrNull()
    }
    assertThat(e.issue).isEqualTo("unexpected field: unstable.version")
  }

  @Test
  fun `readGate repeated unstable`() {
    val e = assertFailsWith<WitException> {
      WitFileReader("@unstable(feature = fancier-foo) @unstable(feature = faster-foo)")
        .readGateOrNull()
    }
    assertThat(e.issue).isEqualTo("unexpected field: unstable.feature")
  }

  @Test
  fun `readGate repeated since`() {
    val e = assertFailsWith<WitException> {
      WitFileReader("@since(version = 0.2.0) @since(version = 0.3.0)").readGateOrNull()
    }
    assertThat(e.issue).isEqualTo("unexpected field: since.version")
  }

  @Test
  fun `readGate repeated deprecated`() {
    val e = assertFailsWith<WitException> {
      WitFileReader("@deprecated(version = 0.2.0) @deprecated(version = 0.3.0)").readGateOrNull()
    }
    assertThat(e.issue).isEqualTo("unexpected field: deprecated.version")
  }

  @Test
  fun `readGate absent`() {
    assertThat(WitFileReader("interface foo {}").readGateOrNull()).isNull()
  }

  @Test
  fun `readInterface success`() {
    val wit = """
      |interface foo {}
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(name = "foo"),
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            documentation = """
              | this is the foo interface
              |it is a good interface
              """.trimMargin(),
            gate = Gate(deprecated = "0.2.2"),
            offset = Offset(4, 1),
            name = "foo",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            offset = Offset(1, 1),
            name = "foo",
            items = listOf(
              Function(
                offset = Offset(2, 3),
                name = "print",
                parameters = listOf(
                  Parameter(
                    offset = Offset(2, 15),
                    name = "message",
                    type = IoTypeName.String,
                  ),
                  Parameter(
                    offset = Offset(2, 32),
                    name = "repeat",
                    type = IoTypeName.Option(IoTypeName.U32),
                  ),
                ),
                returnType = IoTypeName.Result(
                  err = IoTypeName.Declared("errno"),
                ),
              ),
              Function(
                offset = Offset(3, 3),
                name = "async-print",
                async = true,
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        packageName = "wasi:clocks@0.2.9".toPackageName(),
        items = listOf(
          Interface(
            offset = Offset(3, 1),
            name = "wall-clock",
            items = listOf(
              Record(
                offset = Offset(4, 3),
                name = "datetime",
                fields = listOf(
                  Field(
                    offset = Offset(5, 5),
                    name = "seconds",
                    type = IoTypeName.U64,
                  ),
                  Field(
                    offset = Offset(6, 5),
                    name = "nanoseconds",
                    type = IoTypeName.U32,
                  ),
                ),
              ),
              Function(
                offset = Offset(9, 3),
                name = "now",
                returnType = IoTypeName.Declared("datetime"),
              ),
              Function(
                offset = Offset(11, 3),
                name = "resolution",
                returnType = IoTypeName.Declared("datetime"),
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            documentation = """
              | tick tock
              | wall clock
              """.trimMargin(),
            gate = Gate(since = "1.0"),
            offset = Offset(4, 1),
            name = "wall-clock",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            offset = Offset(1, 1),
            name = "wall-clock",
            items = listOf(
              Record(
                documentation = " spacetime",
                gate = Gate(since = "2.0"),
                offset = Offset(4, 3),
                name = "datetime",
                fields = listOf(
                  Field(
                    documentation = " just a second",
                    gate = Gate(since = "3.0"),
                    offset = Offset(7, 5),
                    name = "seconds",
                    type = IoTypeName.U64,
                  ),
                  Field(
                    documentation = " tick",
                    gate = Gate(since = "4.0"),
                    offset = Offset(10, 5),
                    name = "nanoseconds",
                    type = IoTypeName.U32,
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
      |  now: func(
      |    /// True to return a non-decreasing value.
      |    monotonic: bool,
      |  ) -> datetime;
      |}
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            offset = Offset(1, 1),
            name = "wall-clock",
            items = listOf(
              Function(
                documentation = " sample the clock",
                gate = Gate(since = "5.0"),
                offset = Offset(4, 3),
                name = "now",
                parameters = listOf(
                  Parameter(
                    documentation = " True to return a non-decreasing value.",
                    offset = Offset(6, 5),
                    name = "monotonic",
                    type = IoTypeName.Bool,
                  ),
                ),
                returnType = IoTypeName.Declared("datetime"),
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            offset = Offset(1, 1),
            name = "monotonic-clock",
            items = listOf(
              Function(
                offset = Offset(2, 3),
                name = "subscribe-instant",
                parameters = listOf(
                  Parameter(
                    offset = Offset(3, 5),
                    name = "when",
                    type = IoTypeName.Declared("instant"),
                  ),
                ),
                returnType = IoTypeName.Declared("pollable"),
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            offset = Offset(1, 1),
            name = "db",
            items = listOf(
              Resource(
                documentation = " big boi",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                name = "blob",
                functions = listOf(
                  Function(
                    documentation = " makes a new one",
                    gate = Gate(since = "2.0"),
                    offset = Offset(7, 5),
                    constructor = true,
                    name = "constructor",
                    parameters = listOf(
                      Parameter(
                        offset = Offset(7, 17),
                        name = "init",
                        type = IoTypeName.List(IoTypeName.U8),
                      ),
                    ),
                  ),
                  Function(
                    documentation = " puts some bytes",
                    gate = Gate(since = "3.0"),
                    offset = Offset(11, 5),
                    name = "write",
                    parameters = listOf(
                      Parameter(
                        offset = Offset(11, 17),
                        name = "bytes",
                        type = IoTypeName.List(IoTypeName.U8),
                      ),
                    ),
                  ),
                  Function(
                    documentation = " gets some bytes",
                    gate = Gate(since = "4.0"),
                    offset = Offset(15, 5),
                    name = "read",
                    parameters = listOf(
                      Parameter(
                        offset = Offset(15, 16),
                        name = "n",
                        type = IoTypeName.U32,
                      ),
                    ),
                    returnType = IoTypeName.List(IoTypeName.U8),
                  ),
                  Function(
                    documentation = " smashes some blobs together",
                    gate = Gate(since = "5.0"),
                    offset = Offset(19, 5),
                    static = true,
                    name = "merge",
                    parameters = listOf(
                      Parameter(
                        offset = Offset(19, 24),
                        name = "lhs",
                        type = IoTypeName.Borrow(IoTypeName.Declared("blob")),
                      ),
                      Parameter(
                        offset = Offset(19, 43),
                        name = "rhs",
                        type = IoTypeName.Borrow(IoTypeName.Declared("blob")),
                      ),
                    ),
                    returnType = IoTypeName.Declared("blob"),
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            offset = Offset(1, 1),
            name = "db",
            items = listOf(
              Resource(
                offset = Offset(2, 3),
                name = "blob",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            offset = Offset(1, 1),
            name = "db",
            items = listOf(
              Variant(
                documentation = " whats included",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                name = "filter",
                cases = listOf(
                  Case(
                    documentation = " all the things",
                    gate = Gate(since = "2.0"),
                    offset = Offset(7, 5),
                    name = "all",
                  ),
                  Case(
                    documentation = " zilch",
                    gate = Gate(since = "3.0"),
                    offset = Offset(10, 5),
                    name = "none",
                  ),
                  Case(
                    documentation = " one",
                    gate = Gate(since = "4.0"),
                    offset = Offset(13, 5),
                    name = "some",
                    type = IoTypeName.List(IoTypeName.String),
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            offset = Offset(1, 1),
            name = "db",
            items = listOf(
              Flags(
                documentation = " comic character",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                name = "properties",
                flags = listOf(
                  Flag(
                    documentation = " plastic",
                    gate = Gate(since = "2.0"),
                    offset = Offset(7, 5),
                    name = "lego",
                  ),
                  Flag(
                    documentation = " avenger",
                    gate = Gate(since = "3.0"),
                    offset = Offset(10, 5),
                    name = "marvel-superhero",
                  ),
                  Flag(
                    documentation = " naughty",
                    gate = Gate(since = "4.0"),
                    offset = Offset(13, 5),
                    name = "supervillain",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            offset = Offset(1, 1),
            name = "db",
            items = listOf(
              Enum(
                documentation = " Roy G.",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                name = "color",
                cases = listOf(
                  Case(
                    documentation = " #ff0000",
                    gate = Gate(since = "2.0"),
                    offset = Offset(7, 5),
                    name = "red",
                  ),
                  Case(
                    documentation = " #0000ff",
                    gate = Gate(since = "3.0"),
                    offset = Offset(10, 5),
                    name = "blue",
                  ),
                  Case(
                    documentation = " #00ff00",
                    gate = Gate(since = "4.0"),
                    offset = Offset(13, 5),
                    name = "green",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            offset = Offset(1, 1),
            name = "db",
            items = listOf(
              TypeAlias(
                documentation = " So Awesome.",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                name = "my-awesome-u32",
                target = IoTypeName.U32,
              ),
              TypeAlias(
                documentation = " So Complicated.",
                gate = Gate(since = "2.0"),
                offset = Offset(7, 3),
                name = "my-complicated-tuple",
                target = IoTypeName.Tuple(
                  listOf(
                    IoTypeName.U32,
                    IoTypeName.S32,
                    IoTypeName.String,
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
      |  use my:dependency/the-interface@3.0.{
      |    /// we can document use items?!
      |    more,
      |    names as foo
      |  };
      |}
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          Interface(
            offset = Offset(1, 1),
            name = "db",
            items = listOf(
              Use(
                documentation = " Four values.",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                path = "an-interface",
                items = listOf(
                  UseItem(offset = Offset(4, 21), type = "a"),
                  UseItem(offset = Offset(4, 24), type = "list"),
                  UseItem(offset = Offset(4, 30), type = "of"),
                  UseItem(offset = Offset(4, 34), type = "names"),
                ),
              ),
              Use(
                documentation = " One aliased value.",
                gate = Gate(since = "2.0"),
                offset = Offset(7, 3),
                path = "my:dependency/the-interface@3.0",
                items = listOf(
                  UseItem(
                    documentation = " we can document use items?!",
                    offset = Offset(9, 5),
                    type = "more",
                  ),
                  UseItem(
                    offset = Offset(10, 5),
                    type = "names",
                    alias = "foo",
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
  fun `world documentation and gates`() {
    val wit = """
      |/// a printer-scanner-fax thingy
      |@since(version = 1.0)
      |world multi-function-device {
      |}
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            documentation = " a printer-scanner-fax thingy",
            gate = Gate(since = "1.0"),
            offset = Offset(3, 1),
            name = "multi-function-device",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            imports = listOf(
              ExternalUsePath(
                documentation = " The component needs an `error-reporter`",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                path = "error-reporter",
              ),
            ),
            exports = listOf(
              ExternalUsePath(
                documentation = " This also exports an `error-creator`",
                gate = Gate(since = "2.0"),
                offset = Offset(7, 3),
                path = "error-creator",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            imports = listOf(
              ExternalUsePath(
                documentation = " This store is aliased as 'primary'",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                plainName = "primary",
                path = "wasi:keyvalue/store",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            exports = listOf(
              ExternalUsePath(
                documentation = " This store is aliased as 'secondary'",
                gate = Gate(since = "2.0"),
                offset = Offset(4, 3),
                plainName = "secondary",
                path = "wasi:keyvalue/store",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            imports = listOf(
              Interface(
                documentation = " This interface is inline",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                name = "host",
                items = listOf(
                  Function(
                    documentation = " This function is in an inline interface",
                    gate = Gate(since = "2.0"),
                    offset = Offset(7, 5),
                    name = "log",
                    parameters = listOf(
                      Parameter(
                        offset = Offset(7, 15),
                        name = "param",
                        type = IoTypeName.String,
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            exports = listOf(
              Interface(
                documentation = " We can export an inline interface",
                gate = Gate(since = "3.0"),
                offset = Offset(4, 3),
                name = "guest",
                items = listOf(
                  Function(
                    documentation = " A function in an inline interface",
                    gate = Gate(since = "4.0"),
                    offset = Offset(7, 5),
                    name = "scan",
                    parameters = listOf(
                      Parameter(
                        offset = Offset(7, 16),
                        name = "document",
                        type = IoTypeName.String,
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            imports = listOf(
              Function(
                documentation = " This function is inline",
                gate = Gate(since = "4.0"),
                offset = Offset(4, 3),
                name = "log",
                parameters = listOf(
                  Parameter(
                    offset = Offset(4, 20),
                    name = "param",
                    type = IoTypeName.String,
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            exports = listOf(
              Function(
                documentation = " This exported function is inline",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                name = "scan",
                parameters = listOf(
                  Parameter(
                    offset = Offset(4, 21),
                    name = "document",
                    type = IoTypeName.String,
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            imports = listOf(
              ExternalUsePath(
                offset = Offset(2, 3),
                plainName = "two",
                path = "store",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            exports = listOf(
              ExternalUsePath(
                offset = Offset(2, 3),
                plainName = "two",
                path = "store",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            items = listOf(
              Include(
                documentation = " This include is pretty basic.",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                path = "my-world-2",
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
      |  include wasi:io/my-world-1 with {
      |    a as a1,
      |    /// we can document include items?!
      |    b as b1
      |  };
      |}
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            items = listOf(
              Include(
                offset = Offset(2, 3),
                path = "wasi:io/my-world-1",
                items = listOf(
                  IncludeItem(
                    offset = Offset(3, 5),
                    type = "a",
                    alias = "a1",
                  ),
                  IncludeItem(
                    documentation = " we can document include items?!",
                    offset = Offset(5, 5),
                    type = "b",
                    alias = "b1",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          IoInlinePackage(
            documentation = Documentation(" This package is pasted from somewhere else."),
            gate = Gate(since = "1.0"),
            offset = Offset(3, 1),
            name = "local:a".toPackageName(),
            declarations = listOf(
              Interface(
                documentation = " This interface is included in a package.",
                gate = Gate(since = "2.0"),
                offset = Offset(6, 3),
                name = "foo",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          IoTopLevelUse(
            documentation = Documentation(" Use the Wasi HTTP types."),
            gate = Gate(since = "1.0"),
            offset = Offset(3, 1),
            path = "wasi:http/types@1.0.0".toUsePath(),
          ),
          IoTopLevelUse(
            documentation = Documentation(" Use the Wasi HTTP handler also."),
            gate = Gate(since = "2.0"),
            offset = Offset(6, 1),
            path = "wasi:http/handler".toUsePath(),
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          IoInlinePackage(
            offset = Offset(1, 1),
            name = "local:a".toPackageName(),
            declarations = listOf(
              IoTopLevelUse(
                documentation = Documentation(" Use the Wasi HTTP types."),
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                path = "wasi:http/types@1.0.0".toUsePath(),
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          IoInlinePackage(
            offset = Offset(1, 1),
            name = "local:a".toPackageName(),
            declarations = listOf(
              World(
                documentation = " a printer-scanner-fax thingy",
                gate = Gate(since = "1.0"),
                offset = Offset(4, 3),
                name = "multi-function-device",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            items = listOf(
              Record(
                offset = Offset(2, 3),
                name = "datetime",
                fields = listOf(
                  Field(
                    offset = Offset(3, 5),
                    name = "seconds",
                    type = IoTypeName.U64,
                  ),
                  Field(
                    offset = Offset(4, 5),
                    name = "nanoseconds",
                    type = IoTypeName.U32,
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            items = listOf(
              Enum(
                offset = Offset(2, 3),
                name = "color",
                cases = listOf(
                  Case(
                    offset = Offset(3, 5),
                    name = "red",
                  ),
                  Case(
                    offset = Offset(4, 5),
                    name = "blue",
                  ),
                  Case(
                    offset = Offset(5, 5),
                    name = "green",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            items = listOf(
              Flags(
                offset = Offset(2, 3),
                name = "properties",
                flags = listOf(
                  Flag(
                    offset = Offset(3, 5),
                    name = "lego",
                  ),
                  Flag(
                    offset = Offset(4, 5),
                    name = "marvel-superhero",
                  ),
                  Flag(
                    offset = Offset(5, 5),
                    name = "supervillain",
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            items = listOf(
              Resource(
                offset = Offset(2, 3),
                name = "blob",
                functions = listOf(
                  Function(
                    offset = Offset(3, 5),
                    constructor = true,
                    name = "constructor",
                    parameters = listOf(
                      Parameter(
                        offset = Offset(3, 17),
                        name = "init",
                        type = IoTypeName.List(IoTypeName.U8),
                      ),
                    ),
                  ),
                  Function(
                    offset = Offset(4, 5),
                    name = "write",
                    parameters = listOf(
                      Parameter(
                        offset = Offset(4, 17),
                        name = "bytes",
                        type = IoTypeName.List(IoTypeName.U8),
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
  fun `type alias in world`() {
    val wit = """
      |world multi-function-device {
      |  type my-awesome-u32 = u32;
      |}
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            items = listOf(
              TypeAlias(
                offset = Offset(2, 3),
                name = "my-awesome-u32",
                target = IoTypeName.U32,
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            items = listOf(
              Use(
                offset = Offset(2, 3),
                path = "an-interface",
                items = listOf(
                  UseItem(offset = Offset(2, 21), type = "a"),
                  UseItem(offset = Offset(2, 24), type = "list"),
                  UseItem(offset = Offset(2, 30), type = "of"),
                  UseItem(offset = Offset(2, 34), type = "names"),
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
      """.trimMargin().toWitFile()
    assertThat(wit).isEqualTo(
      IoWitFile(
        items = listOf(
          World(
            offset = Offset(1, 1),
            name = "multi-function-device",
            items = listOf(
              Variant(
                offset = Offset(2, 3),
                name = "filter",
                cases = listOf(
                  Case(
                    offset = Offset(3, 5),
                    name = "all",
                  ),
                  Case(
                    offset = Offset(4, 5),
                    name = "none",
                  ),
                  Case(
                    offset = Offset(5, 5),
                    name = "some",
                    type = IoTypeName.List(IoTypeName.String),
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
