package com.splitforge.app.data

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface BillingRepository {
    val state: StateFlow<BillingState>
    suspend fun refresh()
    suspend fun purchasePro()
    suspend fun restore()
}

data class BillingState(
    val isPro: Boolean,
    val isConfigured: Boolean,
    val priceText: String,
    val isBusy: Boolean,
    val message: String?
) {
    companion object {
        fun initial() = BillingState(
            isPro = false,
            isConfigured = false,
            priceText = "",
            isBusy = false,
            message = null
        )
    }
}

class NoopBillingRepository(
    private val cachedPro: Boolean,
    private val priceText: String
) : BillingRepository {
    private val _state = MutableStateFlow(
        BillingState(
            isPro = cachedPro,
            isConfigured = false,
            priceText = priceText,
            isBusy = false,
            message = null
        )
    )
    override val state: StateFlow<BillingState> = _state.asStateFlow()

    override suspend fun refresh() {
        _state.value = _state.value.copy(isPro = cachedPro)
    }

    override suspend fun purchasePro() {
        _state.value = _state.value.copy(message = null)
    }

    override suspend fun restore() {
        _state.value = _state.value.copy(message = null)
    }
}

class RevenueCatBillingRepository(
    private val apiKey: String,
    private val cachedPro: Boolean,
    private val entitlementId: String,
    private val offeringId: String,
    private val configurator: PlatformPurchasesConfigurator
) : BillingRepository {
    private val _state = MutableStateFlow(BillingState.initial().copy(isPro = cachedPro))
    override val state: StateFlow<BillingState> = _state.asStateFlow()
    private var configured = false

    private suspend fun ensureConfigured() {
        if (configured) return
        configurator.configure(apiKey)
        configured = true
    }

    override suspend fun refresh() {
        if (apiKey.isBlank()) return
        _state.value = _state.value.copy(isBusy = true, message = null)
        try {
            ensureConfigured()
            val info = Purchases.sharedInstance.awaitCustomerInfo()
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val price = offerings.current?.availablePackages?.firstOrNull()?.storeProduct?.price?.formatted
            _state.value = _state.value.copy(
                isPro = hasEntitlement(info),
                isConfigured = true,
                priceText = price ?: _state.value.priceText,
                isBusy = false
            )
        } catch (throwable: Throwable) {
            _state.value = _state.value.copy(isBusy = false, message = throwable.message)
        }
    }

    override suspend fun purchasePro() {
        if (apiKey.isBlank()) return
        _state.value = _state.value.copy(isBusy = true, message = null)
        try {
            ensureConfigured()
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val current = offerings.all[offeringId] ?: offerings.current
            val packageToBuy = current?.availablePackages?.firstOrNull()
            if (packageToBuy == null) {
                _state.value = _state.value.copy(isBusy = false, message = "No offering")
                return
            }
            val result = Purchases.sharedInstance.awaitPurchase(packageToBuy)
            _state.value = _state.value.copy(
                isPro = hasEntitlement(result.customerInfo),
                isConfigured = true,
                isBusy = false
            )
        } catch (throwable: Throwable) {
            _state.value = _state.value.copy(isBusy = false, message = throwable.message)
        }
    }

    override suspend fun restore() {
        if (apiKey.isBlank()) return
        _state.value = _state.value.copy(isBusy = true, message = null)
        try {
            ensureConfigured()
            val info = Purchases.sharedInstance.awaitRestore()
            _state.value = _state.value.copy(
                isPro = hasEntitlement(info),
                isConfigured = true,
                isBusy = false
            )
        } catch (throwable: Throwable) {
            _state.value = _state.value.copy(isBusy = false, message = throwable.message)
        }
    }

    private fun hasEntitlement(info: CustomerInfo): Boolean {
        return info.entitlements.active.containsKey(entitlementId)
    }
}
