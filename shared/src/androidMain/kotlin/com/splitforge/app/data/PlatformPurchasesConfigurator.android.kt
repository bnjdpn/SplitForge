package com.splitforge.app.data

import android.content.Context
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.splitforge.app.PlatformContext

actual class PlatformPurchasesConfigurator actual constructor(private val context: PlatformContext?) {
    actual fun configure(apiKey: String) {
        val androidContext = context as? Context ?: return
        val configuration = PurchasesConfiguration.Builder(androidContext, apiKey).build()
        Purchases.configure(configuration)
    }
}
