package com.splitforge.app.ui.strings

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

actual class LanguageProvider {
    actual fun currentLanguage(): AppLanguage {
        val locale = NSLocale.currentLocale
        val code = locale.objectForKey(NSLocaleLanguageCode) as? String
        val language = (code ?: locale.localeIdentifier).lowercase()
        return if (language.startsWith("fr")) AppLanguage.FR else AppLanguage.EN
    }
}
