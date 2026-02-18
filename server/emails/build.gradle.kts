plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.burst)
  id("build-support")
}

wasmoBuild {
  domTester()
}

kotlin {
  js {
    browser()
    useEsModules()
  }
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.okio)
        implementation(libs.kotlinx.html)
        implementation(project(":common:framework"))
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
        implementation(libs.burst.coroutines)
        implementation(libs.kotlin.test.js)
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":dom-tester"))
      }
    }
  }
}
