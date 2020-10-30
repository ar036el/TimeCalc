/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.timecalculator.utils.purchase_manager

import android.app.Activity
import com.arealapps.timecalculator.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalculator.helpers.listeners_engine.ListenersManager
import com.arealapps.timecalculator.utils.preferences_managers.parts.Preference
import com.arealapps.timecalculator.utils.preferences_managers.parts.PreferencesManager
import com.arealapps.timecalculator.utils.purchase_manager.core.BillingEngine
import com.arealapps.timecalculator.utils.purchase_manager.core.PurchaseStatus


interface PurchasableItem : HoldsListeners<PurchasableItem.Listener> {

    val SKU: String
    val price: String
    val purchaseStatus: PurchaseStatus
    fun tryToLaunchBillingFlow(activity: Activity): Boolean
    fun forceDebug(purchaseStatus: PurchaseStatus?)

    interface Listener {
        fun purchaseStatusHasChanged(purchaseStatus: PurchaseStatus)
        fun priceHasChanged(purchaseStatus: String)
    }
}

class PurchasableItemImpl(
    override val SKU: String,
    private val billingEngine: BillingEngine,
    purchasesPreferencesManager: PreferencesManager,
    private val listenersMgr: ListenersManager<PurchasableItem.Listener> = ListenersManager()
): PurchasableItem, HoldsListeners<PurchasableItem.Listener> by listenersMgr {


    override val purchaseStatus: PurchaseStatus
        get() = purchaseStatusDebug ?: prefForPurchaseStatus.value
    override val price: String
        get() = prefForPrice.value

    private val prefForPurchaseStatus = purchasesPreferencesManager.createEnumPref(SKU + "PurchaseStatus", PurchaseStatus.values(),
        PurchaseStatus.UnspecifiedOrNotPurchased
    )
    private val prefForPrice = purchasesPreferencesManager.createStringPref(SKU + "Price", null, "")
    private var purchaseStatusDebug: PurchaseStatus? = null

    override fun tryToLaunchBillingFlow(activity: Activity): Boolean {
        return billingEngine.tryToLaunchBillingFlow(activity, SKU)
    }

    override fun forceDebug(purchaseStatus: PurchaseStatus?) {
        purchaseStatusDebug = purchaseStatus
    }

    init {
        prefForPurchaseStatus.addListener( object : Preference.Listener<PurchaseStatus> {
            override fun prefHasChanged(preference: Preference<PurchaseStatus>, value: PurchaseStatus) {
                listenersMgr.notifyAll { it.purchaseStatusHasChanged(value) }
            }
        })
        prefForPrice.addListener( object : Preference.Listener<String> {
            override fun prefHasChanged(preference: Preference<String>, value: String) {
                listenersMgr.notifyAll { it.priceHasChanged(value) }
            }
        })

        billingEngine.addListener(object : BillingEngine.Listener {
            override fun onPurchaseStatusesLoadedOrChanged(SKUsWithPurchaseStatuses: Map<String, PurchaseStatus>) {
                prefForPurchaseStatus.value = SKUsWithPurchaseStatuses[SKU] ?: PurchaseStatus.UnspecifiedOrNotPurchased
            }
            override fun onPricesLoadedOrChanged(SKUsWithPrices: Map<String, String>) {
                SKUsWithPrices[SKU]?.let { prefForPrice.value = it }
            }
        })
    }

}