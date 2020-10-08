package com.arealapps.timecalc.junitTests

import TimeBlock
import com.arealapps.timecalc.calculation_engine.basics.Num
import com.arealapps.timecalc.calculation_engine.symbol.TimeUnit

class TimeResultLayoutTest {

    class TimeBlockMock(
        override val timeUnit: TimeUnit,
        override var number: Num,
        override var visibilityPercentage: Float,
        override var isMaximizedSymbolVisible: Boolean
    ) : TimeBlock {
        override fun addListener(Listener: TimeBlock.Listener) { throw NotImplementedError() }
        override fun addListeners(vararg Listeners: TimeBlock.Listener) { throw NotImplementedError() }
        override fun removeListener(Listener: TimeBlock.Listener): Boolean { throw NotImplementedError() }
        override fun removeListeners(vararg Listeners: TimeBlock.Listener): Boolean { throw NotImplementedError() }
        override fun clearListeners() { throw NotImplementedError() }
        override fun hasListener(Listener: TimeBlock.Listener): Boolean { throw NotImplementedError() }
    }

}