package com.arealapps.timecalc.calculatorActivity.ui.calculator.ResultLayout

import TimeBlock

interface CollapseMechanism {
    fun tryToCollapseWithAnimation(target: TimeBlock)
    fun tryToRevealWithAnimation(target: TimeBlock)
}

class CollapseMechanismImpl : CollapseMechanism {

}