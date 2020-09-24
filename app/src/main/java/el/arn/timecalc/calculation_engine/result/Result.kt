package el.arn.timecalc.calculation_engine.result

import el.arn.timecalc.calculation_engine.atoms.Num

interface Result

class TimeResult(val totalMillis: Num): Result
class NumberResult(val number: Num) : Result
class MixedResult(val number: Num, val totalMillis: Num): Result

interface ErrorResult : Result
class CantDivideByZeroErrorResult : ErrorResult
class CantMultiplyTimeQuantitiesErrorResult : ErrorResult
class ExpressionIsEmptyErrorResult : ErrorResult
class BadFormulaErrorResult : ErrorResult

