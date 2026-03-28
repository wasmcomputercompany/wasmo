plugins {
  alias(libs.plugins.kotlin.jvm)
  id("org.gradle.application")
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

application {
  applicationName = "moose"
  mainClass.set("com.wasmo.cli.WasmoCommandKt")
}

dependencies {
  implementation(libs.clikt)
  implementation(libs.okio)
  implementation(libs.tomlkt)
  implementation(project(":platform:issues"))
  implementation(project(":platform:packaging"))
  testImplementation(libs.okio.fakefilesystem)
}
