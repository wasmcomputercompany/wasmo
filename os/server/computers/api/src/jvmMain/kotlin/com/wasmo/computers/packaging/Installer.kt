package com.wasmo.computers.packaging

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.issues.IssueCollector
import com.wasmo.packaging.AppManifest

interface Installer {
  context(issueCollector: IssueCollector)
  suspend fun install(): AppManifest?

  interface Factory {
    fun create(
      appSlug: AppSlug,
      wasmoFileAddress: WasmoFileAddress,
    ): Installer
  }
}
