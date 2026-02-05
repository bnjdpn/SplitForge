package com.splitforge.app

import com.russhwolf.settings.NSUserDefaultsSettings
import com.splitforge.app.data.AppStateRepository
import com.splitforge.app.data.DefaultDataFactory
import com.splitforge.app.data.NoopBillingRepository
import com.splitforge.app.data.PlatformPurchasesConfigurator
import com.splitforge.app.data.RevenueCatBillingRepository
import com.splitforge.app.data.RevenueCatConfig
import com.splitforge.app.ui.strings.LanguageProvider
import com.splitforge.app.ui.strings.StringsProvider
import com.splitforge.app.util.IosShareService
import com.splitforge.app.util.IosSummaryImageExporter
import com.splitforge.app.util.SummaryImageExporter
import com.splitforge.app.util.ShareService
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual class PlatformContext

actual class PlatformAppDependenciesFactory actual constructor(context: PlatformContext?) {
    actual fun create(): AppDependencies {
        val strings = StringsProvider.forLanguage(LanguageProvider().currentLanguage())
        val settings = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val defaultState = DefaultDataFactory.create(strings)
        val appStateRepository = AppStateRepository(settings, json, defaultState)
        val cachedPro = appStateRepository.state.value.isProCached
        val configurator = PlatformPurchasesConfigurator(null)
        val billingRepository = if (RevenueCatConfig.apiKey.isBlank()) {
            NoopBillingRepository(cachedPro = cachedPro, priceText = strings.priceUnavailable)
        } else {
            RevenueCatBillingRepository(
                apiKey = RevenueCatConfig.apiKey,
                cachedPro = cachedPro,
                entitlementId = RevenueCatConfig.entitlementId,
                offeringId = RevenueCatConfig.offeringId,
                configurator = configurator
            )
        }
        val summaryImageExporter: SummaryImageExporter = IosSummaryImageExporter(strings)
        val shareService: ShareService = IosShareService()
        return AppDependencies(
            appStateRepository = appStateRepository,
            billingRepository = billingRepository,
            summaryImageExporter = summaryImageExporter,
            shareService = shareService,
            strings = strings
        )
    }
}
