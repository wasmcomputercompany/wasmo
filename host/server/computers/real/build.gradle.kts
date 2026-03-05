plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.burst)
  alias(libs.plugins.metro)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(project(":host:api"))
        implementation(project(":host:framework"))
        implementation(project(":host:server:accounts:api"))
        implementation(project(":host:server:calls:api"))
        implementation(project(":host:server:computers:api"))
        implementation(project(":host:server:db"))
        implementation(project(":host:server:deployment"))
        implementation(project(":host:server:identifiers"))
        implementation(project(":host:server:jobs:api"))
        implementation(project(":host:server:payments:api"))
        implementation(project(":platform:api"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.okio.fakefilesystem)
        implementation(project(":platform:testing"))
        implementation(project(":host:server:testing"))
        implementation(project(":host:server:website:api"))
        implementation(project(":host:server:website:real"))
      }
    }
  }
}
