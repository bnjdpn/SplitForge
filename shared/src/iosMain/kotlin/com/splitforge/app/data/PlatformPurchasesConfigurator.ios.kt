package com.splitforge.app.data

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

actual class PlatformPurchasesConfigurator actual constructor(context: Any?) {
    actual fun configure(apiKey: String) {
        val configuration = PurchasesConfiguration.Builder(apiKey).build()
        Purchases.configure(configuration)
    }
}
