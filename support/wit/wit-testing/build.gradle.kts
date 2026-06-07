plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    jvmMain {
      dependencies {
        implementation(libs.okio)
        implementation(projects.support.wit.witCore)
        implementation(projects.support.wit.witKotlin)
        implementation(projects.support.wit.witKotlinGenerator)
      }
    }
    jvmTest {
      dependencies {
        implementation(libs.okio.fakefilesystem)
      }
    }
  }
}
