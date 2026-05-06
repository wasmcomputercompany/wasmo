import io.freefair.gradle.plugins.sass.SassCompile

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.sass)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

val compileSass = project.tasks.register("compileSass", SassCompile::class.java) {
  source(project.layout.projectDirectory.dir("src/main/scss"))
  includePaths.from(rootProject.layout.projectDirectory.dir("submodules/pico/scss"))
  destinationDir.set(project.layout.buildDirectory.dir("css"))
}
