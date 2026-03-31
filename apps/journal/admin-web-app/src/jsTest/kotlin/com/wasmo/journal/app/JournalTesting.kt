package com.wasmo.journal.app

import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.EntrySummary
import com.wasmo.journal.api.Visibility
import kotlin.time.Instant

fun EntrySnapshot.toSummary() = EntrySummary(
  token = token,
  visibility = visibility,
  slug = slug,
  title = title,
  date = date,
)

object SampleEntries {
  val WasmIsLikeJson = EntrySnapshot(
    token = "aaaaabbbbbcccccdddddeeeee",
    version = 1L,
    visibility = Visibility.Private,
    slug = "wasm",
    title = "WebAssembly is like JSON for behaviour",
    body = """
      |If you’re sending data from one computer to another, it’s probably JSON. But it hasn’t
      |always been this way.
      |
      |How we Transmitted Data Before JSON
      |-----------------------------------
      |
      |Before JSON got popular (2005ish) we had XML, CORBA, Java serialization, Python Pickle,
      |and a bunch of other clever and complex encoding schemes. One thing that Really Sucked
      |about these schemes was getting different platforms to share data.
      |
      |Perhaps my writer program would make an XML file with schemas and namespaces and UTF-8.
      |Perhaps your reader program would ignore all that ceremony and do the basics. And then
      |our data would degrade in transit! Seeing `’` replaced with `â€™` is still a very
      |familiar failure mode.
      |
      |Or perhaps we’d use Java Serialization. That _100% Pure Java_ solution was easy to get
      |started, awful to change, and impossible to secure. This was a standard industry practice
      |in the bad old days!
      |
      |...
      """.trimMargin(),
    date = Instant.fromEpochSeconds(0L),
  )

  val MultipleColumnInClause = EntrySnapshot(
    token = "fffffggggghhhhhiiiiijjjjj",
    version = 1L,
    visibility = Visibility.Private,
    slug = "sql-multiple-column-in-clause",
    title = "SQL Multiple-Column IN Clause",
    body = """
      |The `IN` clause is handy. I use it all of the time.
      |
      |```sql
      |SELECT
      |  *
      |FROM
      |  cars
      |WHERE
      |  make IN ('Ford', 'Subaru') AND
      |  price < 5000;
      |
      |+--------+---------+------+--------+-------+
      || make   | model   | year | color  | price |
      |+--------+---------+------+--------+-------+
      || Ford   | Focus   | 2007 | Black  |  1100 |
      || Ford   | Mustang | 2005 | Yellow |  4000 |
      || Ford   | Fiesta  | 2011 | Yellow |  4500 |
      || Subaru | Legacy  | 2003 | Yellow |   900 |
      || Subaru | Impreza | 2005 | Red    |  1400 |
      |+--------+---------+------+--------+-------+
      |```
      |
      |Today I came across a two-column IN clause in a code review:
      |
      |...
      """.trimMargin(),
    date = Instant.fromEpochSeconds(0L),
  )
}
