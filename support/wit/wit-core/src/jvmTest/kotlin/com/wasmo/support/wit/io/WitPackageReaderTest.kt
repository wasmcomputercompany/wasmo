package com.wasmo.support.wit.io

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import com.wasmo.support.wit.Documentation
import com.wasmo.support.wit.Offset
import com.wasmo.support.wit.WitException
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
      IoWitPackage(
        packageDocumentation = Documentation(" command line interfaces!"),
        packageName = "wasi:cli".toPackageName(),
        files = mapOf(
          "command.wit".toPath() to IoWitFile(
            items = listOf(
              World(
                offset = Offset(1, 1),
                name = "command",
                imports = listOf(
                  ExternalUsePath(
                    offset = Offset(3, 3),
                    path = "exit",
                  ),
                ),
                exports = listOf(
                  ExternalUsePath(
                    offset = Offset(2, 3),
                    path = "run",
                  ),
                ),
              ),
            ),
          ),
          "exit.wit".toPath() to IoWitFile(
            packageDocumentation = Documentation(" command line interfaces!"),
            packageName = "wasi:cli".toPackageName(),
            items = listOf(
              Interface(
                offset = Offset(4, 1),
                name = "exit",
                items = listOf(
                  Function(
                    offset = Offset(5, 3),
                    name = "exit",
                    parameters = listOf(
                      Parameter(
                        offset = Offset(5, 14),
                        name = "status",
                        type = IoTypeName.Result(),
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
          "run.wit".toPath() to IoWitFile(
            items = listOf(
              Interface(
                offset = Offset(1, 1),
                name = "run",
                items = listOf(
                  Function(
                    offset = Offset(2, 3),
                    name = "run",
                    returnType = IoTypeName.Result(),
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
