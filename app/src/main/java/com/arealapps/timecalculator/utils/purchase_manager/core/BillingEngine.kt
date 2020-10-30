/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.timecalculator.utils.purchase_manager.core

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.arealapps.timecalculator.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalculator.helpers.listeners_engine.ListenersManager
import com.arealapps.timecalculator.helpers.native_.EnumWithId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class PurchaseStatus(override val id: String) : EnumWithId { UnspecifiedOrNotPurchased("UnspecifiedOrError"), Pending("Pending"), Purchased("Purchased")}

interface BillingEngine : HoldsListeners<BillingEngine.Listener> {

    fun reloadPurchases(): Boolean /** @return [false] if the billing client is not connected. In that case, it tried to reconnect. Listeners will be notified when purchases will reload*/
    fun reloadPrices(): Boolean /** @return [false] if the billing client is not connected. In that case, it tried to reconnect. Listeners will be notified when purchases will reload*/
    fun tryToLaunchBillingFlow(activity: Activity, SKU: String): Boolean /** @return [true] if successful, [false] otherwise (fails because SKU is not found or hasn't finished loading)*/

        interface Listener {
        /** Is being called when: (1)billing client connects to the server, (2)a purchase was just made, (3)[reloadPurchases] was called.*/
        fun onPurchaseStatusesLoadedOrChanged(SKUsWithPurchaseStatuses: Map<String, PurchaseStatus>) {}
        /** Is being called when: (1)billing client connects to the server, (2)[reloadPrices] was called.*/
        fun onPricesLoadedOrChanged(SKUsWithPrices: Map<String, String>) {}
    }
}

class BillingEngineImpl(
    val SKUs: Set<String>,
    applicationContext: Context,
    private val listenersMgr: ListenersManager<BillingEngine.Listener> = ListenersManager()
) : BillingEngine, HoldsListeners<BillingEngine.Listener> by listenersMgr {

    override fun reloadPurchases(): Boolean /** @return [false] if the billing client is not connected. In that case, it tried to reconnect. Listeners will be notified when purchases will reload*/ {
        if (billingClient.isReady) {
            queryPurchasesAsync()
            return true
        }
        billingClient.tryToConnect()
        return false
    }

    override fun reloadPrices(): Boolean /** @return [false] if the billing client is not connected. In that case, it tried to reconnect. Listeners will be notified when purchases will reload*/ {
        if (billingClient.isReady) {
            querySkuDetailsAsync()
            return true
        }
        billingClient.tryToConnect()
        return false
    }

    override fun tryToLaunchBillingFlow(activity: Activity, SKU: String): Boolean /** @return [true] if successful, [false] otherwise (fails because SKU is not found or hasn't finished loading)*/ {
        val skuDetails = SKUWithSkuDetails[SKU] ?: return false
        val purchaseParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
        billingClient.launchBillingFlow(activity, purchaseParams)
        return true
    }

    private lateinit var billingClient: BillingClient
    private var SKUWithSkuDetails = mutableMapOf<String, SkuDetails>()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
//                BillingClient.BillingResponseCode.OK -> {
//                    // will handle server verification, consumables, and updating the local cache
//                    //TODO just purchased?
//                    processAndDeclarePurchases(purchases)
//                }
                BillingClient.BillingResponseCode.OK, BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    // item already owned? call queryPurchasesAsync to verify and process all such items
                    queryPurchasesAsync()
                }
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                    billingClient.tryToConnect()
                }
            }
        Log.d("billing client", billingResult.debugMessage)
    }

    private fun processPurchases(purchases: List<Purchase>) {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            Log.d("billing client", "processPurchases called")
            val SKUsWithPurchaseStatuses = mutableMapOf<String, PurchaseStatus>()

            purchases.forEach { purchase ->
                var purchaseStatus: PurchaseStatus = PurchaseStatus.UnspecifiedOrNotPurchased
                val sku = purchase.sku
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (isSignatureValid(purchase)) {
                        purchaseStatus =
                            PurchaseStatus.Purchased
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                    } else {
                        Log.w("billing client", "Purchase is not verified!!!: ${purchase.sku}")
                    }

                } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                    Log.d("billing client", "Received a pending purchase of SKU: ${purchase.sku}")
                    purchaseStatus = PurchaseStatus.Pending
                }

                if (purchase.sku !in SKUs || SKUsWithPurchaseStatuses[purchase.sku] != null ) {
                    Log.w("billing client", "unlisted sku ${purchase.sku} detected")
                } else {
                    SKUsWithPurchaseStatuses[purchase.sku] =  purchaseStatus
                }
            }

            listenersMgr.notifyAll { it.onPurchaseStatusesLoadedOrChanged(SKUsWithPurchaseStatuses) }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient.acknowledgePurchase(params) { billingResult -> Log.d("billing client", "acknowledgeNonConsumablePurchasesAsync response is ${billingResult.responseCode} ${billingResult.debugMessage}") }
    }


    private fun querySkuDetailsAsync() {
        val params = SkuDetailsParams.newBuilder().setSkusList(SKUs.toList()).setType(BillingClient.SkuType.INAPP).build()
        Log.d("billing client", "querySkuDetailsAsync for ${BillingClient.SkuType.INAPP}")

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {

                    val SKUsWithPrices = mutableMapOf<String, String>()
                    skuDetailsList.forEach { skuDetails ->

                        if (skuDetails.sku !in SKUs || SKUsWithPrices[skuDetails.sku] != null) {
                            Log.w("billing client", "unlisted sku ${skuDetails.sku} detected")
                        } else {
                            Log.d("billing client", "SKU found: ${skuDetails.sku} with price of ${skuDetails.price}${skuDetails.priceCurrencyCode}")
                            SKUsWithPrices[skuDetails.sku] = skuDetails.price
                            SKUWithSkuDetails[skuDetails.sku] = skuDetails
                        }
                    }

                    listenersMgr.notifyAll { it.onPricesLoadedOrChanged(SKUsWithPrices) }
                }
                else -> {
                    Log.e("billing client", billingResult.debugMessage)
                }
            }
        }
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(
            Security.BASE_64_ENCODED_PUBLIC_KEY,
            purchase.originalJson,
            purchase.signature
        )
    }

    private fun queryPurchasesAsync() {
        Log.d("billing client", "queryPurchasesAsync called")
        val result = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
        Log.d("billing client", "queryPurchasesAsync INAPP results: ${result.purchasesList?.size}")
        // will handle server verification, consumables, and updating the local cache
        processPurchases(result.purchasesList.orEmpty().filterNotNull())
    }

    private fun BillingClient.tryToConnect(): Boolean {
        Log.d("billing client", "connectToPlayBillingService")
        if (!this.isReady) {
            this.startConnection(billingClientStateListener)
            return true
        }
        return false
    }

    private val billingClientStateListener = object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) { //trivial
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d("billing client", "onBillingSetupFinished successfully")
                    querySkuDetailsAsync()
                    queryPurchasesAsync()
                }
                else -> {
                    //do nothing. Someone else will connect it through retry policy. May choose to send to server though
                    Log.d("billing client", billingResult.debugMessage)
                }
            }
        }

        override fun onBillingServiceDisconnected() { //trivial
            Log.d("billing client", "onBillingServiceDisconnected")
            billingClient.tryToConnect()
        }
    }

    init {
        billingClient = BillingClient.newBuilder(applicationContext)
            .enablePendingPurchases()
            .setListener(purchasesUpdatedListener)
            .build()
        billingClient.tryToConnect()
    }

}