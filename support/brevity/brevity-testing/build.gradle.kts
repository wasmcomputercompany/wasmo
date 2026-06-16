plugins {
  id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
  jvm()
  sourceSets {
    jvmMain {
      dependencies {
        implementation(libs.okio)
        implementation(projects.brevityCore)
        implementation(projects.brevityKotlin)
      }
    }
    jvmTest {
      dependencies {
        implementation(libs.assertk)
        implementation(libs.kotlin.test)
        implementation(libs.kotlin.test.junit)
        implementation(libs.okio.fakefilesystem)
      }
    }
  }
}
