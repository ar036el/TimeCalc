package com.arealapps.timecalc.utils

import android.app.Application
import com.arealapps.timecalc.calculation_engine.TimeConverter
import com.arealapps.timecalc.calculation_engine.TimeConverterImpl
import com.arealapps.timecalc.calculation_engine.TimeExpressionFactory
import com.arealapps.timecalc.calculation_engine.converters.*
import com.arealapps.timecalc.utils.config.ConfigManager
import com.arealapps.timecalc.utils.config.ConfigManagerImpl

interface RootUtils {
    val timeConverter: TimeConverter
    val toastManager: ToastManager
    val configManager: ConfigManager
    val timeExpressionFactory: TimeExpressionFactory
    val expressionToStringConverter: ExpressionToStringConverter
    val resultToDatabaseStringConverter: ResultToDatabaseStringConverter
    val resultToReadableStringConverter: ResultToReadableStringConverter
}

class RootUtilsImpl(app: Application) : RootUtils {
    override val timeConverter: TimeConverter = TimeConverterImpl()
    override val toastManager: ToastManager = ToastManagerImpl(app.applicationContext)
    override val configManager: ConfigManager = ConfigManagerImpl()
    override val timeExpressionFactory = TimeExpressionFactory(configManager.getTimeExpressionConfig())
    override val expressionToStringConverter = ExpressionToStringConverterImpl(false, true)
    override val resultToDatabaseStringConverter = ResultToDatabaseStringConverterImpl()
    override val resultToReadableStringConverter = ResultToReadableStringConverterImpl()
}