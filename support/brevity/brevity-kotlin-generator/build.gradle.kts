plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  jvm()
  sourceSets {
    jvmMain {
      dependencies {
        implementation(libs.kotlinpoet)
        implementation(libs.okio)
        implementation(projects.brevityCore)
      }
    }
    jvmTest {
      dependencies {
        implementation(libs.assertk)
        implementation(libs.kotlin.test)
        implementation(libs.kotlin.test.junit)
        implementation(libs.okio.fakefilesystem)
        implementation(projects.brevityTesting)
      }
    }
  }
}
