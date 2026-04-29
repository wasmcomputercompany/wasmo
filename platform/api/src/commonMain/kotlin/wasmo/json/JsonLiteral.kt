package wasmo.json

/**
 * Corresponds to the `json` and `jsonb` types in Postgres.
 *
 * https://www.postgresql.org/docs/current/datatype-json.html
 */
data class JsonLiteral(
  val json: String,
)
