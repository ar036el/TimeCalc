package com.arealapps.timecalculator.calculation_engine.result

import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpression
import com.arealapps.timecalculator.calculation_engine.base.Num

interface Result

class TimeResult(val time: TimeExpression): Result
class NumberResult(val number: Num) : Result
class MixedResult(val number: Num, val time: TimeExpression): Result

interface ErrorResult : Result
class CantDivideByZeroErrorResult : ErrorResult
class CantMultiplyTimeQuantitiesErrorResult : ErrorResult
class ExpressionIsEmptyErrorResult : ErrorResult
class BadFormulaErrorResult : ErrorResult

