package com.splitforge.app

import com.splitforge.app.data.AppStateRepository
import com.splitforge.app.data.BillingRepository
import com.splitforge.app.ui.strings.Strings
import com.splitforge.app.util.SummaryImageExporter
import com.splitforge.app.util.ShareService

expect class PlatformContext

expect class PlatformAppDependenciesFactory {
    constructor(context: PlatformContext? = null)
    fun create(): AppDependencies
}

data class AppDependencies(
    val appStateRepository: AppStateRepository,
    val billingRepository: BillingRepository,
    val summaryImageExporter: SummaryImageExporter,
    val shareService: ShareService,
    val strings: Strings
)
