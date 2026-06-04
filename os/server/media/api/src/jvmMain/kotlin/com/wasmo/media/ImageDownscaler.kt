package com.wasmo.objectstore

import wasmo.objectstore.ObjectStore

interface ImageDownscaler {
  fun scaleTo(sourceImage: bytes, scalingInfo: ScalingInfo): bytes
}
