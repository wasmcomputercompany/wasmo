package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
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
  fun `get all types`() {
    val directories = mutableListOf(
      wasiProposals / "cli/wit",
      wasiProposals / "clocks/wit",
      wasiProposals / "filesystem/wit",
      wasiProposals / "http/wit",
      wasiProposals / "io/wit",
      wasiProposals / "random/wit",
      wasiProposals / "sockets/wit",
    )

    val witPackages = directories.map {
      WitPackageReader(fileSystem).read(it)
    }

    val index = SymbolIndex(witPackages)
    for (witPackage in witPackages) {
      for (ref in witPackage.typeReferences()) {
        val declaredType = ref.typeName as? TypeName.Declared ?: continue
        try {
          index.getType(
            typeName = declaredType,
            location = ref.location,
          )
        } catch (e: IllegalArgumentException) {
          throw IllegalArgumentException(
            "failed to find ${ref.typeName} from ${ref.location.path} at ${ref.location.offset}", e,
          )
        }
      }
    }
  }
}
