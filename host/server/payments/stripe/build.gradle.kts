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
    val jvmMain by getting {
      dependencies {
        implementation(libs.gson)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(libs.stripe)
        implementation(project(":host:api"))
        implementation(project(":host:catalog"))
        implementation(project(":host:framework"))
        implementation(project(":host:logging"))
        implementation(project(":host:server:accounts:api"))
        implementation(project(":host:server:db"))
        implementation(project(":host:server:deployment"))
        implementation(project(":host:server:identifiers"))
        implementation(project(":host:server:payments:api"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":host:server:testing"))
        implementation(project(":host:testing"))
        implementation(project(":platform:testing"))
      }
    }
  }
}
