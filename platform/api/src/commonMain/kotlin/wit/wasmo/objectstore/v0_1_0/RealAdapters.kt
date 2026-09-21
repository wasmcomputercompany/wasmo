package wit.wasmo.objectstore.v0_1_0

import dev.wasmo.brevity.WitAdapter
import wasmo.objectstore.Key
import wit.wasmo.objectstore.v0_1_0.Key as WitKey

internal object RealAdapters : Adapters {
  override val typesKey = object : WitAdapter<WitKey, Key> {
    override fun fromWit(wit: WitKey) = Key(wit.value)

    override fun toWit(value: Key) = WitKey(value.value)
  }
}
