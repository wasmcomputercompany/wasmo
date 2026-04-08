package com.wasmo.packaging

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.wasmo.issues.Issue
import com.wasmo.issues.IssueCollector
import com.wasmo.issues.Severity
import kotlin.test.Test
import kotlinx.serialization.encodeToString
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import okio.openZip

class CreateWasmoFileTest {
  private val fileSystem = FakeFileSystem()

  @Test
  fun happyPath() {
    val manifest = AppManifest(
      target = "https://wasmo.com/sdk/1",
      version = 35,
      external_resource = listOf(
        ExternalResource(
          from = "../../external-sources",
          to = "/www/assets",
          include = listOf("**/*.png"),
        ),
      ),
    )

    fileSystem.createDirectories("/external-sources/graphics".toPath())
    fileSystem.write("/external-sources/graphics/logo.png".toPath()) {
      writeUtf8("I am a logo PNG")
    }
    fileSystem.write("/external-sources/graphics/logo.svg".toPath()) {
      writeUtf8("I am a logo SVG (that isn't included in the file)")
    }

    fileSystem.createDirectories("/sources/music.wasmo".toPath())
    fileSystem.write("/sources/music.wasmo/wasmo-manifest.toml".toPath()) {
      writeUtf8(WasmoToml.encodeToString<AppManifest>(manifest))
    }
    fileSystem.createDirectories("/sources/music.wasmo/www".toPath())
    fileSystem.write("/sources/music.wasmo/www/index.html".toPath()) {
      writeUtf8("I am an HTML page")
    }

    val issues = IssueCollector.collect {
      CreateWasmoFile(
        fileSystem = fileSystem,
        inputDirectory = "/sources/music.wasmo".toPath(),
        outputFile = "/outputs/music.wasmo".toPath(),
      ).execute()
    }

    assertThat(issues).isEmpty()

    val output = fileSystem.openZip("/outputs/music.wasmo".toPath())

    assertThat(output.listRecursively("/".toPath()).toList()).containsExactly(
      "/wasmo-manifest.toml".toPath(),
      "/www".toPath(),
      "/www/assets".toPath(),
      "/www/assets/graphics".toPath(),
      "/www/assets/graphics/logo.png".toPath(),
      "/www/index.html".toPath(),
    )

    assertThat(output.read("/wasmo-manifest.toml".toPath()) { readUtf8() })
      .isEqualTo(
        """
        |target = "https://wasmo.com/sdk/1"
        |version = 35
        |external_resource = [  ]
        """.trimMargin(),
      )
    assertThat(output.read("/www/index.html".toPath()) { readUtf8() })
      .isEqualTo("I am an HTML page")
    assertThat(output.read("/www/assets/graphics/logo.png".toPath()) { readUtf8() })
      .isEqualTo("I am a logo PNG")
  }

  @Test
  fun warningWhenExternalResourcesAreAbsent() {
    val manifest = AppManifest(
      target = "https://wasmo.com/sdk/1",
      version = 35,
      external_resource = listOf(
        ExternalResource(
          from = "../../external-sources",
          to = "/www/assets",
          include = listOf("**/*.png"),
        ),
      ),
    )

    fileSystem.createDirectories("/sources/music.wasmo".toPath())
    fileSystem.write("/sources/music.wasmo/wasmo-manifest.toml".toPath()) {
      writeUtf8(WasmoToml.encodeToString<AppManifest>(manifest))
    }

    val issues = IssueCollector.collect {
      CreateWasmoFile(
        fileSystem = fileSystem,
        inputDirectory = "/sources/music.wasmo".toPath(),
        outputFile = "/outputs/music.wasmo".toPath(),
      ).execute()
    }

    assertThat(issues).containsExactly(
      Issue(
        message = "No files found",
        path = "/external-sources",
        severity = Severity.Warning,
      ),
    )

    val output = fileSystem.openZip("/outputs/music.wasmo".toPath())

    assertThat(output.listRecursively("/".toPath()).toList()).containsExactly(
      "/wasmo-manifest.toml".toPath(),
    )
  }
}
