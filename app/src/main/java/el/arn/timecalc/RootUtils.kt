package el.arn.timecalc

import android.app.Application
import el.arn.timecalc.calculation_engine.TimeConverter
import el.arn.timecalc.calculation_engine.TimeConverterImpl

interface RootUtils {
    val timeConverter: TimeConverter
    val toastManager: ToastManager
}

class RootUtilsImpl(app: Application) : RootUtils {
    override val timeConverter: TimeConverter = TimeConverterImpl()
    override val toastManager: ToastManager = ToastManagerImpl(app.applicationContext)
}