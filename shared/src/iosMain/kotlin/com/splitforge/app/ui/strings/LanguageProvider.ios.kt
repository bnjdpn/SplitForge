package com.splitforge.app.ui.strings

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual class LanguageProvider {
    actual fun currentLanguage(): AppLanguage {
        val language = (NSLocale.currentLocale.languageCode ?: "en").lowercase()
        return if (language.startsWith("fr")) AppLanguage.FR else AppLanguage.EN
    }
}
