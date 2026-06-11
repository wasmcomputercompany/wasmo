package dev.wasmo.brevity.io

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.wasmo.brevity.WitException
import dev.wasmo.brevity.ir.IrMapper
import kotlin.test.Test
import kotlin.test.fail
import okio.FileSystem
import okio.Path.Companion.toPath

class ReadAllWasiFilesTest {
  private val fileSystem = FileSystem.SYSTEM
  private val wasiProposals = "../../../submodules/WASI/proposals".toPath()

  @Test
  fun `parse all files`() {
    var witFileCount = 0
    for (path in fileSystem.listRecursively(wasiProposals)) {
      if (!path.name.endsWith(".wit")) continue

      val witContent = fileSystem.read(path) {
        readUtf8()
      }

      try {
        witContent.toWitFile()
      } catch (e: WitException) {
        fail("decoding $path failed at ${e.offset}: ${e.issue}")
      }

      witFileCount++
    }

    // Confirm we successfully decoded a reasonable number of files. If this fails after updating
    // the WASI submodule, it's probably correct to change this value.
    //
    // But don't change it to 0, that means our paths are out of date.
    assertThat(witFileCount).isEqualTo(57)
  }

  @Test
  fun `map all files`() {
    val directories = mutableListOf(
      wasiProposals / "cli/wit",
      wasiProposals / "clocks/wit",
      wasiProposals / "filesystem/wit",
      wasiProposals / "http/wit",
      wasiProposals / "io/wit",
      wasiProposals / "random/wit",
      wasiProposals / "sockets/wit",
    )

    val ioWitPackages = directories.map {
      IoWitPackageReader(fileSystem).read(it)
    }

    IrMapper(ioWitPackages).map()
  }
}
