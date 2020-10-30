package com.arealapps.timecalculator.junitTests

import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpression
import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpressionConfig
import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpressionImpl
import com.arealapps.timecalculator.calculation_engine.base.TimeVariable
import com.arealapps.timecalculator.calculation_engine.base.toNum
import com.arealapps.timecalculator.calculation_engine.symbol.TimeUnit
import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpressionUtilsImpl
import org.junit.After
import org.junit.Test

class TimeExpressionTest {
    var tester: TimeExpression? = null

    private fun createTimeExpression(totalMillis: Double): TimeExpressionImpl {
        val config = TimeExpressionConfig(30, 365)
        return TimeExpressionImpl(config, toNum(totalMillis))
    }

    @After
    fun tearDown() {
        tester = null
    }


    @Test
    fun getAsCollapsed_Test() {
        val tester = createTimeExpression(2.628e+9)
        println(tester.timeUnits)
        val collapsedUnits = TimeVariable(false, false, false, false, false, true, false, false)
        println(tester.getAsCollapsed(collapsedUnits))

    }

    @Test
    fun main() {
        val config = TimeExpressionConfig(30, 365)
        val timeExpressionUtils = TimeExpressionUtilsImpl(config)

        val a = timeExpressionUtils.convertTimeValues(toNum(8), TimeUnit.Week, TimeUnit.Milli)

        println(createTimeExpression(a.toStringUnformatted().toDouble()).totalMillis)
    }

}
