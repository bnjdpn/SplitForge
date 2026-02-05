package com.splitforge.app.ui.strings

import java.util.Locale

actual class LanguageProvider {
    actual fun currentLanguage(): AppLanguage {
        val language = Locale.getDefault().language.lowercase()
        return if (language.startsWith("fr")) AppLanguage.FR else AppLanguage.EN
    }
}
