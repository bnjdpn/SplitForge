package com.splitforge.app.data

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.splitforge.app.PlatformContext

actual class PlatformPurchasesConfigurator actual constructor(context: PlatformContext?) {
    actual fun configure(apiKey: String) {
        if (Purchases.isConfigured) return
        Purchases.configure(apiKey = apiKey)
    }
}
