/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.timecalc.helpers.native_

/** Used for enum classes that are being consumed for some registry. [id] will be used instead of [toString], so no problems will be caused if we want to refactor the enum's names
 */
interface EnumWithId {
    val id: String
}