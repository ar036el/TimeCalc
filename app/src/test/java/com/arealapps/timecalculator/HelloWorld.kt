package com.arealapps.timecalculator

import com.arealapps.timecalculator.calculation_engine.base.toNum
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat


fun main() {

    val maxDigits = 12

    fun format1(number: String) {
        val bigDecimal = BigDecimal(-3.12345E-51)
        val format = DecimalFormat("0.#####E0")
        println(format.format(bigDecimal))
    }

    fun format2(number: String, scale: Int): String {
        val formatter: NumberFormat = DecimalFormat("0.0E0");
        formatter.roundingMode = RoundingMode.HALF_UP
        formatter.maximumFractionDigits = scale
        return formatter.format(BigDecimal(number))
    }

    var number = "111000000000.000000000000000001"

    number = toNum(number).format().toStringUnformatted()


    var result = ""

    if (number.totalDigits() > maxDigits) {
        var result1 = BigDecimal(number).setScale(maxDigits - number.totalIntegers(), BigDecimal.ROUND_HALF_EVEN)
        if (result1.compareTo(BigDecimal.ZERO) == 0) {
            result = format2(number, maxDigits-1).replace(".0E", "E")
        } else {
            result = result1.toString()
        }
        if (!result.contains('E') && result.totalDigits() > maxDigits) {
            result = format2(result, maxDigits-1).replace(".0E", "E")
        }
    } else {
        result = number
    }

    result = result.replace("+", "")

    if (!result.contains('E')) {
        result = toNum(result).toStringWithGroupingFormatting()
    }


//    println(format(BigDecimal(number), maxDigits))
    println(result)
}


fun String.totalDigits(): Int {
    val decimalDigitIfAny = if (any { it == '.' }) 1 else 0
    val unaryMinusIfAny = if (any { it == '-' }) 1 else 0
    return length - decimalDigitIfAny - unaryMinusIfAny
}

fun String.totalIntegers(): Int {
    return toNum(this).floor().toStringUnformatted().totalDigits()
}
