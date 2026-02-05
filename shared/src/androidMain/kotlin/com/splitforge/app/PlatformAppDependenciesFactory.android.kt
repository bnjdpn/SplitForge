package com.splitforge.app

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import com.splitforge.app.data.AppStateRepository
import com.splitforge.app.data.DefaultDataFactory
import com.splitforge.app.data.NoopBillingRepository
import com.splitforge.app.data.PlatformPurchasesConfigurator
import com.splitforge.app.data.RevenueCatBillingRepository
import com.splitforge.app.data.RevenueCatConfig
import com.splitforge.app.ui.strings.LanguageProvider
import com.splitforge.app.ui.strings.StringsProvider
import com.splitforge.app.util.AndroidShareService
import com.splitforge.app.util.AndroidSummaryImageExporter
import com.splitforge.app.util.SummaryImageExporter
import com.splitforge.app.util.ShareService
import kotlinx.serialization.json.Json

actual class PlatformContext(val context: Context)

actual class PlatformAppDependenciesFactory actual constructor(private val context: PlatformContext?) {
    actual fun create(): AppDependencies {
        val appContext = requireNotNull(context) { "Android context required" }.context
        val strings = StringsProvider.forLanguage(LanguageProvider().currentLanguage())
        val prefs = appContext.getSharedPreferences("splitforge", Context.MODE_PRIVATE)
        val settings = SharedPreferencesSettings(prefs)
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val defaultState = DefaultDataFactory.create(strings)
        val appStateRepository = AppStateRepository(settings, json, defaultState)
        val cachedPro = appStateRepository.state.value.isProCached
        val configurator = PlatformPurchasesConfigurator(PlatformContext(appContext))
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
        val summaryImageExporter: SummaryImageExporter = AndroidSummaryImageExporter(strings)
        val shareService: ShareService = AndroidShareService(appContext)
        return AppDependencies(
            appStateRepository = appStateRepository,
            billingRepository = billingRepository,
            summaryImageExporter = summaryImageExporter,
            shareService = shareService,
            strings = strings
        )
    }
}
