package com.splitforge.app.ui.strings

import platform.Foundation.NSLocale

actual class LanguageProvider {
    actual fun currentLanguage(): AppLanguage {
        val language = NSLocale.preferredLanguages.firstOrNull()?.lowercase() ?: "en"
        return if (language.startsWith("fr")) AppLanguage.FR else AppLanguage.EN
    }
}
