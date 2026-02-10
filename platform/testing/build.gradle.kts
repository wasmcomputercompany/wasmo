plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.okio.fakefilesystem)
        implementation(project(":platform:api"))
        implementation(project(":platform:filesystemobjectstore"))
      }
    }
    commonTest {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
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
