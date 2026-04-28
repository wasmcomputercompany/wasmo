package com.wasmo.installedapps

import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import wasmo.access.Caller

interface InstalledAppHttpService {
  suspend fun execute(
    caller: Caller,
    request: Request,
  ): Response<ResponseBody>
}
