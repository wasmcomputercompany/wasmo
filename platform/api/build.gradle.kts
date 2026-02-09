plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
      }
    }
    commonTest {
      dependencies {
        implementation(libs.assertk)
        implementation(libs.kotlin.test)
      }
    }
    jsTest {
      dependencies {
        implementation(libs.kotlin.test.js)
      }
    }
    jvmTest {
      dependencies {
        implementation(libs.kotlin.test.junit)
      }
    }
  }
}
