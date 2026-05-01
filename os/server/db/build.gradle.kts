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
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(libs.tomlkt)
        implementation(projects.identifiers)
        implementation(projects.os.api)
        implementation(projects.os.framework)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.passkeys.api)
        implementation(projects.os.server.sql.api)
        implementation(projects.platform.api)
        implementation(projects.platform.packaging)
        implementation(projects.support.tokens)
        implementation(projects.wasmox.wasmoxSql)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(projects.os.server.testing)
      }
    }
  }
}
