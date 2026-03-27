package com.wasmo.issues

enum class Severity {
  /** Fail the operation. */
  Fatal,

  /** Alert the operator that the system is degraded but may proceed. */
  Warning,
}

data class Issue(
  val message: String,
  val url: String? = null,
  val path: String? = null,
  val href: String? = null,
  val severity: Severity = Severity.Fatal,
  val exception: Throwable? = null,
) {
  override fun toString() = buildString {
    when (severity) {
      Severity.Fatal -> {}
      Severity.Warning -> append("⚠️")
    }
    if (url != null) {
      append(url)
    }
    if (path != null) {
      if (isNotEmpty()) append(" ")
      append(path)
    }
    if (href != null) {
      if (isNotEmpty()) append(" ")
      append(href)
    }

    if (isNotEmpty()) append(": ")
    append(message)

    if (exception != null) {
      append("\n  ")
      append(exception.stackTraceToString().replace("\n", "\n  "))
    }
  }
}

class IssueCollector @PublishedApi internal constructor(
  private val mutableIssues: MutableList<Issue>,
  private val url: String? = null,
  private val path: String? = null,
  private val href: String? = null,
  private val severity: Severity = Severity.Fatal,
) {
  val issues: List<Issue>
    get() = mutableIssues.toList()

  val hasFatalIssues: Boolean
    get() = mutableIssues.any { it.severity == Severity.Fatal }

  constructor() : this(mutableIssues = mutableListOf())

  private fun copy(
    url: String? = this.url,
    path: String? = this.path,
    href: String? = this.href,
    severity: Severity = this.severity,
  ) = IssueCollector(
    mutableIssues = mutableIssues,
    url = url,
    path = path,
    href = href,
    severity = severity,
  )

  fun add(message: String, exception: Throwable? = null) {
    mutableIssues += Issue(
      message = message,
      url = url,
      path = path,
      href = href,
      severity = severity,
      exception = exception,
    )
  }

  fun severity(severity: Severity) = copy(severity = severity)

  fun url(url: String) = copy(url = url)

  fun path(path: String) = copy(path = path)

  fun href(href: String) = copy(href = this.href?.let { "$it.$href" } ?: href)

  companion object {
    inline fun collect(crossinline block: context(IssueCollector) () -> Unit): List<Issue> {
      val issueCollector = IssueCollector()
      context(issueCollector) {
        block()
      }
      return issueCollector.issues
    }
  }
}

context(issueCollector: IssueCollector)
fun issueCheck(condition: Boolean, message: () -> String) {
  if (!condition) {
    issueCollector.add(message())
  }
}
