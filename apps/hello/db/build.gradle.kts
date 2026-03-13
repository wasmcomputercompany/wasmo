plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.sqldelight)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.okio)
        implementation(project(":platform:api"))
        implementation(project(":platform:sqldelight"))
      }
    }
  }
}

sqldelight {
  databases {
    create("HelloDb") {
      packageName.set("com.wasmo.hello.db")
      dialect("app.cash.sqldelight:postgresql-dialect:${libs.versions.sqldelight.get()}")
      deriveSchemaFromMigrations.set(true)
      generateAsync.set(true)
    }
  }
}
