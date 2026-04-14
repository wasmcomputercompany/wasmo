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
        implementation(libs.burst.coroutines)
        implementation(libs.commons.dbcp2)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.okio)
        implementation(libs.vertx.postgresql)
        implementation(project(":os:server:sql:api"))
        implementation(project(":os:server:sql:real"))
        implementation(project(":platform:api"))
        implementation(project(":support:close-tracker"))
      }
    }
  }
}
