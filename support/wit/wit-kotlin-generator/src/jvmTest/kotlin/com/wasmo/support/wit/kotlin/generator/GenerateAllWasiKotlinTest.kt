package com.wasmo.support.wit.kotlin.generator

import com.wasmo.support.wit.io.WitPackageReader
import com.wasmo.support.wit.ir.IrMapper
import java.io.File
import kotlin.test.Test
import okio.FileSystem
import okio.Path.Companion.toPath

/** This dumps a `.kt` file for all the WASI proposals, for manual inspection. */
class GenerateAllWasiKotlinTest {
  private val fileSystem = FileSystem.SYSTEM
  private val wasiProposals = "../../../submodules/WASI/proposals".toPath()

  @Test
  fun generate() {
    val directories = mutableListOf(
      wasiProposals / "cli/wit",
      wasiProposals / "clocks/wit",
      wasiProposals / "filesystem/wit",
      wasiProposals / "http/wit",
      wasiProposals / "io/wit",
      wasiProposals / "random/wit",
      wasiProposals / "sockets/wit",
    )

    val packageReader = WitPackageReader(fileSystem)
    val ioPackages = directories.map {
      packageReader.read(it)
    }

    val irPackages = IrMapper(ioPackages).map()
    val kotlinMapper = KotlinMapper()
    val apiGenerator = ApiGenerator()

    val directory = File("build/GenerateAllWasiKotlinTest")
    directory.mkdirs()
    for (irPackage in irPackages) {
      val kotlinPackage = kotlinMapper.mapPackage(irPackage)
      val fileSpec = apiGenerator.generate(kotlinPackage)
      fileSpec.writeTo(directory)
    }
  }
}
