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
