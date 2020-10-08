package com.arealapps.timecalc

import com.arealapps.timecalc.calculation_engine.*
import com.arealapps.timecalc.helpers.native_.initOnce
import com.arealapps.timecalc.utils.RootUtils
import com.arealapps.timecalc.utils.RootUtilsImpl

lateinit var appRoot: AppRoot
lateinit var rootUtils: RootUtils
//@AcraCore(
//    buildConfigClass = BuildConfig::class,
//    reportSenderFactoryClasses = [CustomReportSenderFactory::class]
//)
class AppRoot : android.app.Application() {

    var calculatorCoordinator: CalculatorCoordinator by initOnce()

    override fun onCreate() {
        super.onCreate()
        appRoot = this
        rootUtils = RootUtilsImpl(this)
    }
//
//    override fun attachBaseContext(base: Context?) {
//        super.attachBaseContext(base)
//        ACRA.init(this)
//    }

}