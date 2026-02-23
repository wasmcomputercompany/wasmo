package com.wasmo.website

import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody

interface AppPageFactory {
  fun create(): Response<ResponseBody>
}
