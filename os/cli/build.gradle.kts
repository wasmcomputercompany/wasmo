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
  implementation(project(":platform:packaging"))
  implementation(project(":support:issues"))
  testImplementation(libs.okio.fakefilesystem)
}
