package com.arealapps.timecalc.helpers.native_

open class Point<T : Number>(open val x: T, open val y: T)
class MutablePoint<T : Number>(override var x: T, override var y: T) : Point<T>(x, y)
class PxPoint(override val x: Float, override val y: Float) : Point<Float>(x, y)



@JvmName("minusInt")
operator fun Point<Int>.minus(other: Point<Int>) = Point(this.x - other.x, this.y - other.y)
@JvmName("minusFloat")
operator fun Point<Float>.minus(other: Point<Float>) = Point(this.x - other.x, this.y - other.y)
@JvmName("minusDouble")
operator fun Point<Double>.minus(other: Point<Double>) = Point(this.x - other.x, this.y - other.y)



@JvmName("plusInt")
operator fun Point<Int>.plus(other: Point<Int>) = Point(this.x + other.x, this.y + other.y)
@JvmName("plusFloat")
operator fun Point<Float>.plus(other: Point<Float>) = Point(this.x + other.x, this.y + other.y)
@JvmName("plusDouble")
operator fun Point<Double>.plus(other: Point<Double>) = Point(this.x + other.x, this.y + other.y)
