plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    jvmMain {
      dependencies {
        implementation(libs.clikt)
        implementation(libs.okio)
        implementation(libs.tomlkt)
        implementation(project(":platform:issues"))
        implementation(project(":platform:packaging"))
      }
    }
    jvmTest {
      dependencies {
        implementation(libs.okio.fakefilesystem)
      }
    }
  }
}
