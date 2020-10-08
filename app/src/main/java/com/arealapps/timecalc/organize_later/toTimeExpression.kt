package com.arealapps.timecalc.organize_later

import com.arealapps.timecalc.calculation_engine.basics.Num
import com.arealapps.timecalc.calculation_engine.basics.toNum
import com.arealapps.timecalc.rootUtils

fun Num.toTimeExpression() = rootUtils.timeExpressionFactory.createTimeExpression(this)