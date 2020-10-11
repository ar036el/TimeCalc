package com.arealapps.timecalc.junitTests

import com.arealapps.timecalc.calculation_engine.timeExpression.TimeExpression
import com.arealapps.timecalc.calculation_engine.timeExpression.TimeExpressionConfig
import com.arealapps.timecalc.calculation_engine.timeExpression.TimeExpressionImpl
import com.arealapps.timecalc.calculation_engine.basics.TimeVariable
import com.arealapps.timecalc.calculation_engine.basics.toNum
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
}