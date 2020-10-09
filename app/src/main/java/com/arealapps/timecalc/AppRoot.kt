package com.arealapps.timecalc

import com.arealapps.timecalc.utils.RootUtils
import com.arealapps.timecalc.utils.RootUtilsImpl

lateinit var appRoot: AppRoot
lateinit var rootUtils: RootUtils
//@AcraCore(
//    buildConfigClass = BuildConfig::class,
//    reportSenderFactoryClasses = [CustomReportSenderFactory::class]
//)
class AppRoot : android.app.Application() {

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