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
        implementation(libs.sqldelight.sqlite.driver)
      }
    }
  }
}

sqldelight {
  databases {
    create("HelloDb") {
      packageName.set("com.wasmo.hello.db")
      deriveSchemaFromMigrations.set(true)
    }
  }
}
