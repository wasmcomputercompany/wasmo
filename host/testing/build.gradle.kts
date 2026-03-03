plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.metro)
}

kotlin {
  js {
    browser()
  }
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlinx.serialization.json)
        implementation(project(":host:api"))
        implementation(project(":host:logging"))
      }
    }
  }
}
