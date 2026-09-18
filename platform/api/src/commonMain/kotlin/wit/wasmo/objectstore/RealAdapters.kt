package wit.wasmo.objectstore

import dev.wasmo.brevity.WitAdapter
import wit.wasmo.objectstore.Key as WitKey

internal object RealAdapters : Adapters {
  override val typesKey = object : WitAdapter<WitKey, wasmo.objectstore.Key> {
    override fun fromWit(wit: WitKey) = wasmo.objectstore.Key(wit.value)

    override fun toWit(value: wasmo.objectstore.Key) = WitKey(value.value)
  }
}
