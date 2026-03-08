package com.wasmo.packaging

data class Issue(
  val context: String?,
  val message: String,
)

class IssueCollector private constructor(
  val context: String? = null,
  val issues: MutableList<Issue> = mutableListOf(),
) {
  fun add(message: String) {
    issues += Issue(context, message)
  }

  fun issueCheck(condition: Boolean, message: () -> String) {
    if (!condition) {
      add(message())
    }
  }

  fun withContext(
    context: String,
    block: IssueCollector.() -> Unit,
  ) {
    val next = IssueCollector(
      context = this.context?.let { "$it.$context" } ?: context,
      issues = issues,
    )
    next.block()
  }

  companion object {
    fun collect(block: IssueCollector.() -> Unit): List<Issue> {
      val issueCollector = IssueCollector()
      issueCollector.block()
      return issueCollector.issues
    }
  }
}
