package com.splitforge.app.ui.strings

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleLanguageCode

actual class LanguageProvider {
    actual fun currentLanguage(): AppLanguage {
        val code = NSLocale.currentLocale.objectForKey(NSLocaleLanguageCode) as? String
        val language = (code ?: NSLocale.currentLocale.localeIdentifier).lowercase()
        return if (language.startsWith("fr")) AppLanguage.FR else AppLanguage.EN
    }
}
