import com.wasmo.gradle.WasmoFileTask

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

val filesWasmo by tasks.registering(WasmoFileTask::class) {
  inputDirectory.set(File(projectDir, "files"))
  outputDirectory.set(layout.buildDirectory.dir("wasmo/files"))
  wasmoFilePath.set("static/files/files.wasmo")
}
val libraryWasmo by tasks.registering(WasmoFileTask::class) {
  inputDirectory.set(File(projectDir, "library"))
  outputDirectory.set(layout.buildDirectory.dir("wasmo/library"))
  wasmoFilePath.set("static/library/library.wasmo")
}
val musicWasmo by tasks.registering(WasmoFileTask::class) {
  inputDirectory.set(File(projectDir, "music"))
  outputDirectory.set(layout.buildDirectory.dir("wasmo/music"))
  wasmoFilePath.set("static/music/music.wasmo")
}
val photosWasmo by tasks.registering(WasmoFileTask::class) {
  inputDirectory.set(File(projectDir, "photos"))
  outputDirectory.set(layout.buildDirectory.dir("wasmo/photos"))
  wasmoFilePath.set("static/photos/photos.wasmo")
}
val recipesWasmo by tasks.registering(WasmoFileTask::class) {
  inputDirectory.set(File(projectDir, "recipes"))
  outputDirectory.set(layout.buildDirectory.dir("wasmo/recipes"))
  wasmoFilePath.set("static/recipes/recipes.wasmo")
}
val smartWasmo by tasks.registering(WasmoFileTask::class) {
  inputDirectory.set(File(projectDir, "smart"))
  outputDirectory.set(layout.buildDirectory.dir("wasmo/smart"))
  wasmoFilePath.set("static/smart/smart.wasmo")
}
val snakeWasmo by tasks.registering(WasmoFileTask::class) {
  inputDirectory.set(File(projectDir, "snake"))
  outputDirectory.set(layout.buildDirectory.dir("wasmo/snake"))
  wasmoFilePath.set("static/snake/snake.wasmo")
}
val writerWasmo by tasks.registering(WasmoFileTask::class) {
  inputDirectory.set(File(projectDir, "writer"))
  outputDirectory.set(layout.buildDirectory.dir("wasmo/writer"))
  wasmoFilePath.set("static/writer/writer.wasmo")
}
val zapWasmo by tasks.registering(WasmoFileTask::class) {
  inputDirectory.set(File(projectDir, "zap"))
  outputDirectory.set(layout.buildDirectory.dir("wasmo/zap"))
  wasmoFilePath.set("static/zap/zap.wasmo")
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      resources.srcDir(filesWasmo)
      resources.srcDir(libraryWasmo)
      resources.srcDir(musicWasmo)
      resources.srcDir(photosWasmo)
      resources.srcDir(recipesWasmo)
      resources.srcDir(smartWasmo)
      resources.srcDir(snakeWasmo)
      resources.srcDir(writerWasmo)
      resources.srcDir(zapWasmo)
    }
  }
}
