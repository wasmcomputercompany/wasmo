package com.wasmo.computers.packaging

import com.wasmo.issues.IssueCollector
import com.wasmo.packaging.AppManifest

internal class FileSystemInstaller : Installer {
  context(issueCollector: IssueCollector)
  override suspend fun install(): AppManifest? {
    TODO()
  }
}
