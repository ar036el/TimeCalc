package el.arn.timecalc.tests

import TimeBlock
import androidx.constraintlayout.widget.ConstraintLayout
import el.arn.timecalc.calculation_engine.TimeConverter
import el.arn.timecalc.calculation_engine.TimeConverterImpl
import el.arn.timecalc.calculation_engine.atoms.Num
import el.arn.timecalc.calculation_engine.result.ResultBuilder
import el.arn.timecalc.calculation_engine.result.ResultBuilderImpl
import el.arn.timecalc.calculation_engine.result.TimeResult
import el.arn.timecalc.calculation_engine.symbol.TimeUnit
import el.arn.timecalc.helpers.assert.pass
import el.arn.timecalc.mainActivity.ui.TimeResultUI
import el.arn.timecalc.mainActivity.ui.TimeResultUILogic
import org.junit.After
import org.junit.Before
import org.junit.Test

class TimeResultUITest {

    class TimeBlockMock(
        override val timeUnit: TimeUnit,
        override var currentNumber: Num,
        override val originalNumber: Num,
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