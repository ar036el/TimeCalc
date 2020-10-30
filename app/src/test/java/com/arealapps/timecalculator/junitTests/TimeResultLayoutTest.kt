package com.arealapps.timecalculator.junitTests

import TimeBlock
import android.view.View
import com.arealapps.timecalculator.calculation_engine.base.Num
import com.arealapps.timecalculator.calculation_engine.symbol.TimeUnit

class TimeResultLayoutTest {

    class TimeBlockMock(
        override val timeUnit: TimeUnit,
        override var number: Num,
        override var visibilityPercentage: Float,
        override var isMaximizedSymbolVisible: Boolean
    ) : TimeBlock {
        override fun getBlockView(): View {
            TODO("Not yet implemented")
        }

        override fun addListener(Listener: TimeBlock.Listener) { throw NotImplementedError() }
        override fun addListeners(vararg Listeners: TimeBlock.Listener) { throw NotImplementedError() }
        override fun removeListener(Listener: TimeBlock.Listener): Boolean { throw NotImplementedError() }
        override fun removeListeners(vararg Listeners: TimeBlock.Listener): Boolean { throw NotImplementedError() }
        override fun clearListeners() { throw NotImplementedError() }
        override fun hasListener(Listener: TimeBlock.Listener): Boolean { throw NotImplementedError() }
    }

}