plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  js {
    browser()
    useEsModules()
  }
  jvm()

  sourceSets {
    val commonMain by getting {
      dependencies {
        compileOnly(libs.jetbrains.annotations)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(project(":common:api"))
        implementation(project(":common:framework"))
        implementation(project(":common:logging"))
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core.js)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(libs.assertk)
        implementation(libs.kotlin.test)
        implementation(project(":common:testing"))
      }
    }
    val jsTest by getting {
      dependencies {
        implementation(libs.kotlin.test.js)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlin.test.junit)
      }
    }
  }
}
