package com.wasmo.computers

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.issues.IssueCollector
import com.wasmo.packaging.AppManifest

interface ResourceInstaller {
  context(issueCollector: IssueCollector)
  suspend fun install(): AppManifest?

  interface Factory {
    fun create(
      appSlug: AppSlug,
      wasmoFileAddress: WasmoFileAddress,
    ): ResourceInstaller
  }
}
