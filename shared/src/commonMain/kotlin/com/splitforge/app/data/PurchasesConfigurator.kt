package com.splitforge.app.data

import com.splitforge.app.PlatformContext

expect class PlatformPurchasesConfigurator(context: PlatformContext?) {
    fun configure(apiKey: String)
}
