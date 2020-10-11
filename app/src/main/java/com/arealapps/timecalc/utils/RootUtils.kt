package com.arealapps.timecalc.utils

import android.app.Application
import com.arealapps.timecalc.calculation_engine.timeExpression.TimeExpressionUtils
import com.arealapps.timecalc.calculation_engine.converters.*
import com.arealapps.timecalc.calculation_engine.timeExpression.TimeExpressionUtilsImpl
import com.arealapps.timecalc.utils.config.ConfigManager
import com.arealapps.timecalc.utils.config.ConfigManagerImpl
import com.arealapps.timecalc.utils.preferences_managers.CalculatorPreferencesManager

interface RootUtils {
    val toastManager: ToastManager
    val configManager: ConfigManager
    val timeExpressionUtils: TimeExpressionUtils
    val expressionToStringConverter: ExpressionToStringConverter
    val resultToDatabaseStringConverter: ResultToDatabaseStringConverter
    val resultToReadableStringConverter: ResultToReadableStringConverter
    val calculatorPreferencesManager: CalculatorPreferencesManager
}

class RootUtilsImpl(app: Application) : RootUtils {
    override val toastManager: ToastManager = ToastManagerImpl(app.applicationContext)
    override val configManager: ConfigManager = ConfigManagerImpl()
    override val timeExpressionUtils: TimeExpressionUtils = TimeExpressionUtilsImpl(configManager.getTimeExpressionConfig())
    override val expressionToStringConverter = ExpressionToStringConverterImpl(false, true)
    override val resultToDatabaseStringConverter = ResultToDatabaseStringConverterImpl()
    override val resultToReadableStringConverter = ResultToReadableStringConverterImpl()
    override val calculatorPreferencesManager = CalculatorPreferencesManager()
}