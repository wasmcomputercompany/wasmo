package com.wasmo.computers.packaging

import com.wasmo.identifiers.AppManifestAddress
import com.wasmo.issues.Issue
import com.wasmo.packaging.AppManifest

interface Installer {
  suspend fun install(): InstallResult

  interface Factory {
    fun create(manifestAddress: AppManifestAddress): Installer
  }
}

sealed interface InstallResult {
  data class Success(
    val manifest: AppManifest,
  )

  data class Failure(
    val issues: List<Issue>,
  )
}
