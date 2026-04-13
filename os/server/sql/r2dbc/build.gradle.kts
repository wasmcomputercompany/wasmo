plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.burst)
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
        implementation(libs.kotlinx.coroutines.reactive)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(libs.r2dbc)
        implementation(libs.r2dbc.postgresql)
        implementation(libs.reactive.streams)
        implementation(project(":os:server:sql:api"))
        implementation(project(":platform:api"))
        implementation(project(":support:close-tracker"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.burst.coroutines)
        implementation(project(":os:server:testing"))
      }
    }
  }
}
