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
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(libs.sqldelight.jdbc.driver)
        implementation(libs.tomlkt)
        implementation(project(":identifiers"))
        implementation(project(":os:api"))
        implementation(project(":os:framework"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:passkeys:api"))
        implementation(project(":os:server:sql:jdbc"))
        implementation(project(":platform:api"))
        implementation(project(":platform:packaging"))
        implementation(project(":support:tokens"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(project(":os:server:testing"))
      }
    }
  }
}
