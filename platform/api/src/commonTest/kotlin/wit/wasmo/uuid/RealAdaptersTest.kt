@file:OptIn(ExperimentalUuidApi::class)

package wit.wasmo.uuid

import com.wasmo.assertRoundTrip
import kotlin.test.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RealAdaptersTest {
  @Test
  fun `typesUuid v4`() {
    RealAdapters.typesUuid.assertRoundTrip(
      Uuid(6463201931226794266UL to 13045984721966408817UL),
      Uuid.fromLongs(6463201931226794266L, -5400759351743142799L),
    )
  }

  @Test
  fun `typesUuid v7`() {
    RealAdapters.typesUuid.assertRoundTrip(
      Uuid(117280729187382370UL to 11579919183008054260UL),
      Uuid.fromLongs(117280729187382370L, -6866824890701497356L),
    )
  }
}
