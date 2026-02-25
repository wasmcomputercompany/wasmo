plugins {
  alias(libs.plugins.kotlin.multiplatform)
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
