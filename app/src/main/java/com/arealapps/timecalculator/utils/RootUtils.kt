package com.arealapps.timecalculator.utils

import android.app.Application
import com.arealapps.timecalculator.appRoot
import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpressionUtils
import com.arealapps.timecalculator.calculation_engine.converters.*
import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpressionUtilsImpl
import com.arealapps.timecalculator.utils.tutoriaShowcase.TutorialShowcaseManager
import com.arealapps.timecalculator.utils.tutoriaShowcase.TutorialShowcaseManagerImpl
import com.arealapps.timecalculator.utils.config.ConfigManager
import com.arealapps.timecalculator.utils.config.ConfigManagerImpl
import com.arealapps.timecalculator.utils.preferences_managers.CalculatorPreferencesManager
import com.arealapps.timecalculator.utils.purchase_manager.PurchasesManager
import com.arealapps.timecalculator.utils.purchase_manager.PurchasesManagerImpl

interface RootUtils {
    val timeExpressionUtils: TimeExpressionUtils

    val expressionToStringConverter: ExpressionToStringConverter
    val toastManager: ToastManager
    val configManager: ConfigManager
    val resultToDatabaseStringConverter: ResultToDatabaseStringConverter
    val resultToReadableStringConverter: ResultToReadableStringConverter
    val calculatorPreferencesManager: CalculatorPreferencesManager
    val activityInitUtils: ActivityInitUtils
    val vibrationManager: VibrationManager
    val purchasesManager: PurchasesManager
    val tutorialShowcaseManager: TutorialShowcaseManager
}

class RootUtilsImpl(app: Application) : RootUtils {
    override val calculatorPreferencesManager = CalculatorPreferencesManager()
    override val configManager: ConfigManager = ConfigManagerImpl(calculatorPreferencesManager)
    override val timeExpressionUtils: TimeExpressionUtils = TimeExpressionUtilsImpl(configManager.getTimeExpressionConfig())
    override val expressionToStringConverter = ExpressionToStringConverterImpl(false, true)
    override val toastManager: ToastManager = ToastManagerImpl(app.applicationContext)
    override val resultToDatabaseStringConverter = ResultToDatabaseStringConverterImpl(timeExpressionUtils)
    override val resultToReadableStringConverter = ResultToReadableStringConverterImpl()
    override val activityInitUtils = ActivityInitUtilsImpl(calculatorPreferencesManager)
    override val vibrationManager = VibrationManagerImpl()
    override val purchasesManager = PurchasesManagerImpl(appRoot.applicationContext)
    override val tutorialShowcaseManager = TutorialShowcaseManagerImpl()
}