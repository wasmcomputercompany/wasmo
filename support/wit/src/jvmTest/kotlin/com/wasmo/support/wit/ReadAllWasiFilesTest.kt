package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.fail
import okio.FileSystem
import okio.Path.Companion.toPath

class ReadAllWasiFilesTest {
  private val fileSystem = FileSystem.SYSTEM
  private val wasiProposals = "../../submodules/WASI/proposals".toPath()

  @Test
  fun test() {
    var witFileCount = 0
    for (path in fileSystem.listRecursively(wasiProposals)) {
      if (!path.name.endsWith(".wit")) continue

      val witContent = fileSystem.read(path) {
        readUtf8()
      }

      val witReader = WitReader(witContent)
      try {
        witReader.read()
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
}
