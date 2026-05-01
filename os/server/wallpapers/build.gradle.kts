plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.burst)
  id("wasmo-build")
  id("dom-tester")
}

wasmoBuild {
  libraryJvmJs()
}

domTester {
  domTester()
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.okio)
      }
    }
    jsTest {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.kotlinx.html)
        implementation(projects.support.domTester)
        implementation(projects.support.okioHtml)
      }
    }
  }
}
