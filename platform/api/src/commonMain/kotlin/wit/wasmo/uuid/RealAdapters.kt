package wit.wasmo.uuid

import dev.wasmo.brevity.WitAdapter
import kotlin.uuid.Uuid as KotlinUuid

internal object RealAdapters : Adapters {
  override val typesUuid = object : WitAdapter<Uuid, KotlinUuid> {
    override fun fromWit(wit: Uuid) =
      KotlinUuid.fromLongs(wit.value.first.toLong(), wit.value.second.toLong())

    override fun toWit(value: KotlinUuid) =
      value.toLongs { mostSignificantBits, leastSignificantBits ->
        Uuid(mostSignificantBits.toULong() to leastSignificantBits.toULong())
      }
  }
}
