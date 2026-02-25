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
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(project(":common:api"))
        implementation(project(":common:framework"))
        implementation(project(":common:tokens"))
        implementation(project(":platform:api"))
        implementation(project(":server:accounts:api"))
        implementation(project(":server:db"))
        implementation(project(":server:deployment"))
        implementation(project(":server:emails"))
        implementation(project(":server:identifiers"))
        implementation(project(":server:passkeys:api"))
        implementation(project(":server:sendemail:api"))
        implementation(project(":server:website:api"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":common:testing"))
        implementation(project(":platform:testing"))
        implementation(project(":server:testing"))
      }
    }
  }
}
