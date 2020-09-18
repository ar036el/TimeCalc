package el.arn.timecalc

import el.arn.timecalc.calculator_core.calculation_engine.*

lateinit var appRoot: AppRoot
//@AcraCore(
//    buildConfigClass = BuildConfig::class,
//    reportSenderFactoryClasses = [CustomReportSenderFactory::class]
//)
class AppRoot : android.app.Application() {

    lateinit var calculatorCoordinator: CalculatorCoordinator



    override fun onCreate() {
        super.onCreate()
        appRoot = this


        calculatorCoordinator = CalculatorCoordinatorImpl()

        initAllVars()
    }
//
//    override fun attachBaseContext(base: Context?) {
//        super.attachBaseContext(base)
//        ACRA.init(this)
//    }

    private fun initAllVars() {
    }

}