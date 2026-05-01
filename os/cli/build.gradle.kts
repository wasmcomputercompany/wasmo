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
  implementation(libs.clikt.core)
  implementation(libs.okio)
  implementation(projects.platform.packaging)
  implementation(projects.support.issues)
}
