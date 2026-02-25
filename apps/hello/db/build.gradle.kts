plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.sqldelight)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

dependencies {
  implementation(libs.okio)
  implementation(libs.sqldelight.sqlite.driver)
}

sqldelight {
  databases {
    create("HelloDb") {
      packageName.set("com.wasmo.hello.db")
      deriveSchemaFromMigrations.set(true)
    }
  }
}
