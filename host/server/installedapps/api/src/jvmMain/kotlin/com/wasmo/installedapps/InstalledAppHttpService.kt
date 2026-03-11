package com.wasmo.installedapps

import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody

interface InstalledAppHttpService {
  suspend fun execute(request: Request): Response<ResponseBody>
}
