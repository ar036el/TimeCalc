/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.timecalculator.utils.purchase_manager

import android.content.Context
import com.arealapps.timecalculator.utils.purchase_manager.core.GenericPurchasesManager
import com.arealapps.timecalculator.utils.purchase_manager.core.GenericPurchasesManagerImpl
import com.arealapps.timecalculator.utils.purchase_manager.core.PurchaseStatus

interface PurchasesManager : GenericPurchasesManager {
    val purchasedNoAds: Boolean
    val noAds: PurchasableItem
    enum class PurchasableItems { NoAds }
}


class PurchasesManagerImpl (
    applicationContext: Context
) : PurchasesManager, GenericPurchasesManagerImpl(setOf(noAdsSKU), applicationContext)  {

    private companion object {
        const val noAdsSKU = "no_ads_0"
    }

    override val noAds = getPurchasableItem(noAdsSKU)

    override val purchasedNoAds: Boolean
        get() {
            return (noAds.purchaseStatus == PurchaseStatus.Purchased)
        }
}