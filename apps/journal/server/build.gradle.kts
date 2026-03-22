plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.burst)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
  consumeJsResources("static/pink/assets")
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.html)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.r2dbc.postgresql)
        implementation(project(":apps:journal:api"))
        implementation(project(":apps:journal:db"))
        implementation(project(":os:tokens"))
        implementation(project(":platform:api"))
        implementation(project(":platform:sqldelight"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.commons.dbcp2)
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":platform:testing"))
        implementation(project(":os:server:sql:api"))
        implementation(project(":os:server:sql:r2dbc"))
        implementation(project(":os:server:testing"))
      }
    }
  }
}

dependencies {
  add("jsResources", project(":apps:journal:client"))
}
