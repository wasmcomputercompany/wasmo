package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertFailsWith
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

class WitPackageReaderTest {
  @Test
  fun happyPath() {
    val directory = "/my-package".toPath()
    val fileSystem = FakeFileSystem()
    fileSystem.createDirectories(directory)
    fileSystem.write(directory / "command.wit") {
      writeUtf8(
        """
        |world command {
        |  export run;
        |  import exit;
        |}
        """.trimMargin(),
      )
    }
    fileSystem.write(directory / "exit.wit") {
      writeUtf8(
        """
        |/// command line interfaces!
        |package wasi:cli;
        |
        |interface exit {
        |  exit: func(status: result);
        |}
        """.trimMargin(),
      )
    }
    fileSystem.write(directory / "run.wit") {
      writeUtf8(
        """
        |interface run {
        |  run: func() -> result;
        |}
        """.trimMargin(),
      )
    }
    val packageReader = WitPackageReader(fileSystem)
    val witPackage = packageReader.read(directory)

    assertThat(witPackage).isEqualTo(
      WitPackage(
        packageDocumentation = Documentation(" command line interfaces!"),
        packageName = "wasi:cli".toPackageName(),
        files = mapOf(
          "command.wit".toPath() to WitFile(
            declarations = listOf(
              World(
                location = Location(1, 1),
                name = "command",
                imports = listOf(
                  ExternalUsePath(
                    location = Location(3, 3),
                    path = "exit",
                  ),
                ),
                exports = listOf(
                  ExternalUsePath(
                    location = Location(2, 3),
                    path = "run",
                  ),
                ),
              ),
            ),
          ),
          "exit.wit".toPath() to WitFile(
            packageDocumentation = Documentation(" command line interfaces!"),
            packageName = "wasi:cli".toPackageName(),
            declarations = listOf(
              Interface(
                location = Location(4, 1),
                name = "exit",
                declarations = listOf(
                  Function(
                    location = Location(5, 3),
                    name = "exit",
                    parameters = listOf(
                      Parameter(
                        location = Location(5, 14),
                        name = "status",
                        type = TypeName.Result(),
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
          "run.wit".toPath() to WitFile(
            declarations = listOf(
              Interface(
                location = Location(1, 1),
                name = "run",
                declarations = listOf(
                  Function(
                    location = Location(2, 3),
                    name = "run",
                    returnType = TypeName.Result(),
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
  fun `conflicting package names`() {
    val directory = "/my-package".toPath()
    val fileSystem = FakeFileSystem()
    fileSystem.createDirectories(directory)
    fileSystem.write(directory / "command.wit") {
      writeUtf8(
        """
        |package wasi:cli@1.0;
        """.trimMargin(),
      )
    }
    fileSystem.write(directory / "exit.wit") {
      writeUtf8(
        """
        |package wasi:cli@2.0;
        """.trimMargin(),
      )
    }

    val packageReader = WitPackageReader(fileSystem)
    val e = assertFailsWith<WitException> {
      packageReader.read(directory)
    }
    assertThat(e).hasMessage(
      """
      |multiple different package names in /my-package/*.wit:
      |  wasi:cli@1.0
      |  wasi:cli@2.0
      """.trimMargin(),
    )
  }

  @Test
  fun `absent package name`() {
    val directory = "/my-package".toPath()
    val fileSystem = FakeFileSystem()
    fileSystem.createDirectories(directory)
    fileSystem.write(directory / "command.wit") {
      writeUtf8(
        """
        |interface command {
        |}
        """.trimMargin(),
      )
    }
    fileSystem.write(directory / "exit.wit") {
      writeUtf8(
        """
        |interface exit {
        |}
        """.trimMargin(),
      )
    }

    val packageReader = WitPackageReader(fileSystem)
    val e = assertFailsWith<WitException> {
      packageReader.read(directory)
    }
    assertThat(e).hasMessage("no package declaration in /my-package/*.wit")
  }
}
