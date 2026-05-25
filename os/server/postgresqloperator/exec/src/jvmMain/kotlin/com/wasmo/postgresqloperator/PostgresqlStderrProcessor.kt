package com.wasmo.postgresqloperator

import com.wasmo.events.EventListener
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.BufferedSource
import okio.ByteString.Companion.encodeUtf8

/**
 * Consume the stderr stream from the `postgres` daemon, configured with this specific logging
 * config.
 *
 * ```
 * log_line_prefix = '> %u@%d %e '
 * ```
 *
 * This allows us to identify where logs came from, to best route them to the applications that
 * issued them.
 *
 * The biggest challenge here is that log messages may contain inline `\n` characters, which are
 * ambiguous. We attempt to mitigate this by preferring to emit on a `\n>` two-character set, but
 * will log if the stream ends with a '\n'. This favors liveness over correctness.
 */
@Inject
@SingleIn(OsScope::class)
internal class PostgresqlStderrProcessor(
  private val eventListener: EventListener,
) {
  fun processAll(source: BufferedSource) {
    while (!source.exhausted()) {
      val nextSplit = source.indexOf(Split)
      if (nextSplit != -1L) {
        val log = source.readUtf8(nextSplit)
        eventListener.onEvent(log.toEvent())
        source.skip(1L) // Consume newline.
        continue
      }

      if (source.buffer[source.buffer.size - 1L] == Newline) {
        val log = source.readUtf8(source.buffer.size - 1L)
        eventListener.onEvent(log.toEvent())
        source.skip(1L) // Consume newline.
        continue
      }

      source.request(source.buffer.size + 1L)
    }
  }

  private fun String.toEvent(): PostgresqlLogEvent {
    val match = MessageRegex.matchEntire(this)
      ?: return PostgresqlLogEvent(message = this)

    return PostgresqlLogEvent(
      user = match.groups[1]?.value?.takeIf { it.isNotEmpty() },
      database = match.groups[2]?.value?.takeIf { it.isNotEmpty() },
      sqlStateErrorCode = match.groups[3]?.value?.takeIf { it.isNotEmpty() && it != "00000" },
      severity = match.groups[4]?.value?.takeIf { it.isNotEmpty() },
      message = match.groups[5]?.value ?: this,
    )
  }

  private companion object {
    val MessageRegex = Regex("""> (\S*)@(\S*) (\S*) (\S*):\s+(.*)""", RegexOption.DOT_MATCHES_ALL)
    val Newline = '\n'.code.toByte()
    val Split = "\n>".encodeUtf8()
  }
}
