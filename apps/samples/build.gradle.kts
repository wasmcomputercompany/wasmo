plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

val filesDotWasmo = wasmoBuild.createWasmoFileTask("files")
val libraryDotWasmo = wasmoBuild.createWasmoFileTask("library")
val musicDotWasmo = wasmoBuild.createWasmoFileTask("music")
val photosDotWasmo = wasmoBuild.createWasmoFileTask("photos")
val recipesDotWasmo = wasmoBuild.createWasmoFileTask("recipes")
val smartDotWasmo = wasmoBuild.createWasmoFileTask("smart")
val snakeDotWasmo = wasmoBuild.createWasmoFileTask("snake")
val writerDotWasmo = wasmoBuild.createWasmoFileTask("writer")
val zapDotWasmo = wasmoBuild.createWasmoFileTask("zap")

kotlin {
  sourceSets {
    val jvmMain by getting {
      resources.srcDir(filesDotWasmo)
      resources.srcDir(libraryDotWasmo)
      resources.srcDir(musicDotWasmo)
      resources.srcDir(photosDotWasmo)
      resources.srcDir(recipesDotWasmo)
      resources.srcDir(smartDotWasmo)
      resources.srcDir(snakeDotWasmo)
      resources.srcDir(writerDotWasmo)
      resources.srcDir(zapDotWasmo)
    }
  }
}
