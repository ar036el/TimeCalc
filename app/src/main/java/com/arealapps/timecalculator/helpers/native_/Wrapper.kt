package com.arealapps.timecalculator.helpers.native_

open class Wrapper<T>(open val obj: T)
open class MutableWrapper<T>(override var obj: T) : Wrapper<T>(obj)