package dev.wasmo.brevity.gradle

import dev.wasmo.brevity.io.IoWitPackageReader
import dev.wasmo.brevity.ir.IrMapper
import dev.wasmo.brevity.kotlin.generator.ApiGenerator
import dev.wasmo.brevity.kotlin.generator.KtMapper
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class BrevityTask : DefaultTask() {

  /** Each directory should contain a single .wit package. */
  @get:InputFiles
  @get:SkipWhenEmpty
  @get:IgnoreEmptyDirectories
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val inputWitPackageDirectories: ConfigurableFileCollection

  @get:OutputDirectory
  internal abstract val outputKotlinCommonMain: DirectoryProperty

  @TaskAction
  fun execute() {
    val packageReader = IoWitPackageReader(FileSystem.SYSTEM)
    val ktMapper = KtMapper()
    val apiGenerator = ApiGenerator()

    val ioWitPackages = inputWitPackageDirectories.map {
      packageReader.read(it.toOkioPath())
    }

    val commonMainDir = outputKotlinCommonMain.get().asFile
    commonMainDir.mkdirs()

    val irPackages = IrMapper(ioWitPackages).map()
    for (irPackage in irPackages) {
      val ktPackage = ktMapper.map(irPackage)
      val fileSpec = apiGenerator.generate(ktPackage)
      fileSpec.writeTo(commonMainDir)
    }
  }
}
