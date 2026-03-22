package com.wasmo.objectstore

import wasmo.objectstore.ObjectStore

interface ObjectStoreConnector {
  fun tryConnect(address: ObjectStoreAddress): ObjectStore?
}
