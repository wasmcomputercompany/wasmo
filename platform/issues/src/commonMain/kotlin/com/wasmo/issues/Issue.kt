package com.wasmo.issues

data class Issue(
  val message: String,
  val url: String? = null,
  val path: String? = null,
  val href: String? = null,
  val exception: Throwable? = null,
)

class IssueCollector @PublishedApi internal constructor(
  val issues: MutableList<Issue>,
  val url: String? = null,
  val path: String? = null,
  val href: String? = null,
) {
  constructor() : this(issues = mutableListOf())

  private fun copy(
    url: String? = this.url,
    path: String? = this.path,
    href: String? = this.href,
  ) = IssueCollector(
    issues = issues,
    url = url,
    path = path,
    href = href,
  )

  fun add(message: String, exception: Throwable? = null) {
    issues += Issue(
      message = message,
      url = url,
      path = path,
      href = href,
      exception = exception,
    )
  }

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
