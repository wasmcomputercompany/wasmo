package com.wasmo.identifiers

import com.wasmo.issues.Issue

interface Event {
  val computerSlug: ComputerSlug?
    get() = null
  val appSlug: AppSlug?
    get() = null
  val issues: List<Issue>
    get() = listOf()
}
