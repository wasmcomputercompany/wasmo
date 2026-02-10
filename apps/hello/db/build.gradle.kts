plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.sqldelight)
}

dependencies {
  implementation(libs.okio)
  implementation(libs.sqldelight.sqlite.driver)
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test.junit)
}

sqldelight {
  databases {
    create("HelloDb") {
      packageName.set("com.wasmo.hello.db")
      deriveSchemaFromMigrations.set(true)
    }
  }
}
