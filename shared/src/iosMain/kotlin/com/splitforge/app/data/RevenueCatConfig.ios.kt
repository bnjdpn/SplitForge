package com.splitforge.app.data

import platform.Foundation.NSBundle

actual object RevenueCatConfig {
    actual val apiKey: String = (NSBundle.mainBundle.objectForInfoDictionaryKey("RevenueCatApiKey") as? String) ?: ""
    actual val entitlementId: String = "pro"
    actual val offeringId: String = "default"
}
