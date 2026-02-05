package com.splitforge.app.data

import com.splitforge.app.shared.BuildConfig

actual object RevenueCatConfig {
    actual val apiKey: String = BuildConfig.REVENUECAT_API_KEY
    actual val entitlementId: String = "pro"
    actual val offeringId: String = "default"
}
