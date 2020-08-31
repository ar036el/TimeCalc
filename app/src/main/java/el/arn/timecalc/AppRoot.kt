package el.arn.timecalc

import el.arn.timecalc.calculator_core.calculation_engine.Expression
import el.arn.timecalc.calculator_core.calculation_engine.ExpressionImpl
import el.arn.timecalc.calculator_core.calculation_engine.ExpressionStringAdapter
import el.arn.timecalc.calculator_core.calculation_engine.ExpressionStringAdapterImpl

lateinit var appRoot: AppRoot
//@AcraCore(
//    buildConfigClass = BuildConfig::class,
//    reportSenderFactoryClasses = [CustomReportSenderFactory::class]
//)
class AppRoot : android.app.Application() {

    lateinit var expression: Expression
    lateinit var expressionStringAdapter: ExpressionStringAdapter



    override fun onCreate() {
        super.onCreate()
        appRoot = this
        expression = ExpressionImpl()
        expressionStringAdapter = ExpressionStringAdapterImpl(expression, false, true)
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