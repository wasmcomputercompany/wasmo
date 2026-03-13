plugins {
  alias(libs.plugins.kotlin.multiplatform)
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
        implementation(project(":host:server:sql:api"))
        implementation(project(":platform:api"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":host:server:testing"))
      }
    }
  }
}
