package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Ignore
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
        fail("decoding $path failed at ${e.location}: ${e.issue}")
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
  @Ignore("many SymbolResolver features aren't implemented yet")
  fun `resolve all types`() {
    val directories = mutableListOf(
      wasiProposals / "cli/wit",
      wasiProposals / "clocks/wit",
      wasiProposals / "filesystem/wit",
      wasiProposals / "http/wit",
      wasiProposals / "io/wit",
      wasiProposals / "random/wit",
      wasiProposals / "sockets/wit",
    )

    val witFiles = directories
      .flatMap { fileSystem.listRecursively(it) }
      .filter { it.name.endsWith(".wit") }
      .associateWith {
        fileSystem.read(it) {
          readUtf8().toWitFile()
        }
      }

    val resolver = SymbolResolver(witFiles.values.toList())
    for ((path, witFile) in witFiles) {
      for (ref in witFile.typeReferences()) {
        val declaredType = ref.typeName as? TypeName.Declared ?: continue
        try {
          resolver.resolveType(
            typeName = declaredType,
            inPackageName = ref.packageName,
            inInterfaceName = ref.interfaceName,
          )
        } catch (e: IllegalArgumentException) {
          throw IllegalArgumentException(
            "failed to resolve ${ref.typeName} from $path at ${ref.location}", e
          )
        }
      }
    }
  }
}
