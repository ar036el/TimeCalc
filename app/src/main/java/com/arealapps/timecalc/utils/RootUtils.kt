package com.arealapps.timecalc.utils

import android.app.Application
import com.arealapps.timecalc.calculation_engine.timeExpression.TimeExpressionUtils
import com.arealapps.timecalc.calculation_engine.converters.*
import com.arealapps.timecalc.calculation_engine.timeExpression.TimeExpressionUtilsImpl
import com.arealapps.timecalc.utils.config.ConfigManager
import com.arealapps.timecalc.utils.config.ConfigManagerImpl
import com.arealapps.timecalc.utils.preferences_managers.CalculatorPreferencesManager

interface RootUtils {
    val timeExpressionUtils: TimeExpressionUtils

    val expressionToStringConverter: ExpressionToStringConverter
    val toastManager: ToastManager
    val configManager: ConfigManager
    val resultToDatabaseStringConverter: ResultToDatabaseStringConverter
    val resultToReadableStringConverter: ResultToReadableStringConverter
    val calculatorPreferencesManager: CalculatorPreferencesManager
}

class RootUtilsImpl(app: Application) : RootUtils {
    override val calculatorPreferencesManager = CalculatorPreferencesManager()
    override val configManager: ConfigManager = ConfigManagerImpl(calculatorPreferencesManager)
    override val timeExpressionUtils: TimeExpressionUtils = TimeExpressionUtilsImpl(configManager.getTimeExpressionConfig())
    override val expressionToStringConverter = ExpressionToStringConverterImpl(false, true)
    override val toastManager: ToastManager = ToastManagerImpl(app.applicationContext)
    override val resultToDatabaseStringConverter = ResultToDatabaseStringConverterImpl(timeExpressionUtils)
    override val resultToReadableStringConverter = ResultToReadableStringConverterImpl()
}