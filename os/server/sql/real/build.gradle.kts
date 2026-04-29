plugins {
  alias(libs.plugins.kotlin.multiplatform)
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
        implementation(libs.commons.dbcp2)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.okio)
        implementation(libs.vertx.postgresql)
        implementation(project(":identifiers"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:sql:api"))
        implementation(project(":platform:api"))
        implementation(project(":support:close-tracker"))
        implementation(project(":wasmox:wasmox-sql"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.burst.coroutines)
        implementation(project(":os:server:sql:testing"))
        implementation(project(":os:server:testing"))
      }
    }
  }
}
