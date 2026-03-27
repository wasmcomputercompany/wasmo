package com.wasmo.packaging

/**
 * A pattern to select files in a directory tree.
 *
 * `*` matches any sequence of characters in a path segment (no slashes)
 * `**` matches any number of path segments (including none).
 *
 * Matching is case-insensitive.
 *
 * If the input contains '/' at the beginning, that is ignored.
 *
 * This is implemented by converting the pattern to a regex. Otherwise, handling patterns like
 * 'a/**/b' is particularly difficult. In particular, we'd need NFA stuff to handle inputs like
 * 'a/b' (matches), 'a/b/' (doesn't match), and 'a/b/b' (matches).
 */
class IncludePattern(
  val pattern: String,
) {
  init {
    require(!pattern.startsWith("/")) { "Unexpected pattern: $pattern" }
  }

  private val regex: Regex by lazy {
    Regex(
      buildString {
        append("/?") // Consume a leading '/' on the input, if any.

        var first = true
        for (pathSegment in pattern.split("/")) {
          // '**' wants a trailing '/' at the beginning of a pattern, and a leading '/' otherwise.
          if (pathSegment == "**") {
            append(
              when {
                first -> "(.*/)?"
                else -> "(/.*)?"
              },
            )
            continue
          }

          if (!first) {
            append("/") // For the preceding '/' in split("/").
          }

          for ((namePartIndex, namePart) in pathSegment.split("*").withIndex()) {
            if (namePartIndex > 0) {
              append("[^/]*") // For the preceding '*' in split("*").
            }
            if (namePart.isNotEmpty()) {
              append(Regex.escape(namePart))
            }
            first = false
          }
        }
      },
      RegexOption.IGNORE_CASE,
    )
  }

  fun matches(path: String) = regex.matches(path)
}
