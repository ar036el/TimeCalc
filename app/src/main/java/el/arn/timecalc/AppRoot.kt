package el.arn.timecalc

import el.arn.timecalc.calculation_engine.*
import el.arn.timecalc.helpers.native_.initOnce
import el.arn.timecalc.utils.RootUtils
import el.arn.timecalc.utils.RootUtilsImpl

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