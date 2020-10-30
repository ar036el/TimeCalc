package com.arealapps.timecalculator.draftsOrFutureWork.layoutResizerExtension
//
//import android.content.Context
//import android.graphics.Color
//import android.os.Bundle
//import android.util.AttributeSet
//import android.view.*
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.arealapps.timecalculator.R
//import kotlinx.android.synthetic.main.activity_main.*
//
//
//var View.heightByLayoutParams: Int
//    get() = this.height
//    set(value) {
//        val layoutParams = this.layoutParams
//        layoutParams.height = value
//        this.layoutParams = layoutParams
//    }
//var View.widthByLayoutParams: Int
//    get() = this.width
//    set(value) {
//        val layoutParams = this.layoutParams
//        layoutParams.width = value
//        this.layoutParams = layoutParams
//    }
//
//fun View.setSizeByLayoutParams(width: Int, height: Int) {
//    val layoutParams = this.layoutParams
//    layoutParams.width = width
//    layoutParams.height
//    this.layoutParams = layoutParams
//}
//
//fun View.doWhenDynamicVariablesAreReady(function: (it: View) -> Unit) { //todo does it work multiple times on same view?
//    viewTreeObserver.addOnGlobalLayoutListener(object :
//        ViewTreeObserver.OnGlobalLayoutListener {
//        override fun onGlobalLayout() {
//            this@doWhenDynamicVariablesAreReady.viewTreeObserver.removeOnGlobalLayoutListener(this)
//            function.invoke(this@doWhenDynamicVariablesAreReady)
//        }
//    })
//    requestLayout()
//}
//
//fun View.doOnLayoutChange(function: (it: View) -> Unit) { //todo does it work multiple times on same view?
//    viewTreeObserver.addOnGlobalLayoutListener { function.invoke(this@doOnLayoutChange) }
//}
//
//
//val scale = 0.9f
//
//
//
//class MainActivity : AppCompatActivity() {
//
//    val checkBox: CheckBox by lazy { findViewById(R.id.checkbox2) }
//    val checkboxContainer: ViewGroup by lazy { findViewById(R.id.checkboxContainer) }
//
//    lateinit var historyDatabaseManager: HistoryDatabaseManager
//    lateinit var recyclerView: RecyclerView
//
//    val contentFixedScale: ViewGroup by lazy { findViewById(R.id.fixedScaleContent) }
//    val containerFixedScale: ViewGroup by lazy { findViewById(R.id.fixedScaleContainer) }
//    lateinit var fixedScale: FixedScalableLayoutExtension<ViewGroup>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        updateSize()
//
//        fixedScale = FixedScalableLayoutExtensionImpl(
//            containerFixedScale,
//            contentFixedScale
//        )
//
//    }
//
//    var counter = 0
//
//    var scalo = 1f
//
//    val View.paddingX: Int get() = paddingStart + paddingEnd
//    val View.paddingY: Int get() = paddingTop + paddingBottom
//
//    private fun updateSize() {
//        checkBox.doWhenDynamicVariablesAreReady {
//            checkBox.scaleX = scalo
//            checkBox.scaleY = scalo
//            checkboxContainer.widthByLayoutParams = (checkBox.width*scalo).toInt() + checkboxContainer.paddingX
//            checkboxContainer.heightByLayoutParams = (checkBox.height*scalo).toInt() + checkboxContainer.paddingY
//            checkbox2.invalidate()
//            checkbox2.requestLayout()
//        }
//    }
//
//    private var faa = ""
//
//    fun addChar(view: View) {
//        checkBox.text = checkBox.text.toString()+"w"
//        faa += "1"
//        contentFixedScale.findViewById<Button>(R.id.button).text = faa
//        updateSize()
//    }
//    fun removeChar(view: View) {
//        faa = faa.substring(0, faa.lastIndex - 1)
//        contentFixedScale.findViewById<Button>(R.id.button).text = faa
//
//        checkBox.text = checkBox.text.toString().substring(
//            0,
//            checkBox.text.toString().lastIndex - 1
//        )
//        updateSize()
//
//
//    }
//
//
//    fun trello(view: View) {
//        (fixedScale as FixedScalableLayoutExtensionImpl).apply { trello = !trello }
//    }
//
//    fun scaleDown(view: View) {
//        println("aaa width:${view.width} height:${view.height}")
//        scalo -= 0.05f
//        updateSize()
//        fixedScale.scale*=0.9f
//    }
//
//    fun scaleUp(view: View) {
//        scalo += 0.05f
//        updateSize()
//        fixedScale.scale*=1.1f
//    }
//
//    fun dontPressMeOld(view: View) {
//        historyDatabaseManager.add(
//            System.currentTimeMillis(),
//            counter++.toString(),
//            "counter $counter"
//        )
//        recyclerView.adapter?.notifyDataSetChanged()
//
//
//        val stretchy = findViewById<LinearLayout>(R.id.stretchy)
//
//        println("haho width " + stretchy.width + " height " + stretchy.height)
//        stretchy.scaleX += 0.1f
//        stretchy.scaleY += 0.1f
//    }
//
//        fun dataDeletoOld(view: View) {
//        //dialog: are you sure do you want to clear all history?
//        //clear history, cancel
//        historyDatabaseManager.clear()
//        recyclerView.adapter?.notifyDataSetChanged()
//    }
//
//}
//
//interface FixedScalableLayoutExtension<T : View> {
//    val content: T
//    /** not smaller that 0*/
//    var scale: Float
//}
//
//fun errorIf(errorMessage: String, predicate: () -> Boolean) {
//    if (predicate.invoke()) {
//        throw InternalError(errorMessage)
//    }
//}
//
//class FixedScalableLayoutExtensionImpl<T : View>(
//    /** must contain only content */
//    private val container: ViewGroup,
//    override val content: T
//) : FixedScalableLayoutExtension<T> {
//
//
//    override var scale = 0f
//        set(value) {
//            errorIf("scale < 0"){ scale < 0}
//            field = value
//            _setScale(value)
//        }
//    private fun updateScale() { scale = scale }
//    private fun initScale() { scale = 1f }
//
//    private val CONTAINER_MAX_SIZE = 5000
//
//    private val containerTop: LinearLayout
//    private val containerMid: LinearLayout
//    private val containerBottom: ContentHolder
//
//
//    private fun _setScale(scale: Float) {
//        content.doWhenDynamicVariablesAreReady {
//            containerTop.widthByLayoutParams = (containerBottom.width * scale).toInt()
//            containerTop.heightByLayoutParams = (containerBottom.height * scale).toInt()
//            containerTop.doWhenDynamicVariablesAreReady {
//                containerBottom.scaleX = scale
//                containerBottom.scaleY = scale
//            }
//        }
//    }
//
//    private fun initComponents() {
//        containerTop.gravity = Gravity.CENTER
//        containerMid.gravity = Gravity.CENTER
//        containerMid.layoutParams = ViewGroup.LayoutParams(CONTAINER_MAX_SIZE, CONTAINER_MAX_SIZE)
//        containerBottom.gravity = Gravity.CENTER
//        containerBottom.layoutParams = ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//
//        container.removeView(content)
//        containerBottom.addView(content)
//        containerMid.addView(containerBottom)
//        containerTop.addView(containerMid)
//        container.addView(containerTop)
//    }
//
//    var trello = false
//
//    init {
//        if (container.childCount != 1 || content.parent != container) {
//            throw InternalError("container have only 1 child - the content")
//        }
//
//        val context = container.context
//        containerTop = LinearLayout(context)
//        containerTop.setBackgroundColor(Color.parseColor("#177fff")) //ocean
//        containerMid = LinearLayout(context)
//        containerMid.setBackgroundColor(Color.parseColor("#572aa3")) //purble
//        containerBottom = ContentHolder(context)
//        containerBottom.setBackgroundColor(Color.parseColor("#ffa1cb")) //pink
//
//        initComponents()
//
//        initScale()
//
//
//
//        containerBottom.doOnSizeChanged = { width: Int, height: Int, oldWidth: Int, oldHeight: Int ->
//            val oldo = containerBottom.width
//            if (!trello) {
////                containerTop.widthByLayoutParams = (width * scale).toInt()
////                containerTop.heightByLayoutParams = (height * scale).toInt()
//
//                containerBottom.x += oldWidth - width
//                containerTop.layoutParams = LinearLayout.LayoutParams((width * scale).toInt(), (height * scale).toInt())
//                containerTop.doWhenDynamicVariablesAreReady {
//                    it.heightByLayoutParams = 300
//                    containerBottom.x -= oldWidth - width
//                }
//                containerTop.invalidate()
//                containerTop.postInvalidate()
//            } else {
//                containerTop.widthByLayoutParams += 100
//                containerTop.heightByLayoutParams += 100
//            }
//            println("okA $width ${content.width} ${containerBottom.width} $oldo")
//
//        }
//
//        containerBottom.doOnLayoutChange {
//            println("qa1 " + it.width)
//        }
//
//        container.doOnLayoutChange {
//            println("qa2 " + it.width)
//            println("qa3 " + containerBottom.width)
//        }
//
//    }
//
//
//
//    private class ContentHolder : LinearLayout {
//        constructor(context: Context) : super(context)
//        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
//        constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
//            context,
//            attrs,
//            defStyle
//        )
//
//        var doOnSizeChanged: ((width: Int, height: Int, oldWidth: Int, oldHeight: Int) -> Unit)? = null
//
//        override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
//            super.onSizeChanged(width, height, oldWidth, oldHeight)
//            doOnSizeChanged?.invoke(width, height, oldWidth, oldHeight)
//        }
//    }
//
//}
//
//
//
//    class MyAdapter(private val databaseManager: HistoryDatabaseManager) :
//    RecyclerView.Adapter<MyAdapter.Item>() {
//
//    // Provide a reference to the views for each data item
//    // Complex data items may need more than one view per item, and
//    // you provide access to all the views for a data item in a view holder.
//    // Each data item is just a string in this case that is shown in a TextView.
//    class Item(val layout: ViewGroup) : RecyclerView.ViewHolder(layout)
//
//
//    // Create new views (invoked by the layout manager)
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int
//    ): Item {
//        val itemLayout = LayoutInflater.from(parent.context)
//            .inflate(R.layout.artifact_history_item, parent, false) as ViewGroup
//        // set the view's size, margins, paddings and layout parameters
//
//        return Item(itemLayout)
//    }
//
//    override fun onBindViewHolder(holder: Item, position: Int) {
//
//        val itemLayout = holder.layout
//
//        val layoutAsEmpty: ViewGroup = itemLayout.findViewById(R.id.item_empty)
//        val layoutAsNormal: ViewGroup = itemLayout.findViewById(R.id.item_normal)
//
//        if (databaseManager.isEmpty()) {
//            layoutAsEmpty.visibility = View.VISIBLE
//            layoutAsNormal.visibility = View.GONE
//        } else {
//            layoutAsEmpty.visibility = View.GONE
//            layoutAsNormal.visibility = View.VISIBLE
//
//            itemLayout.findViewById<TextView>(R.id.textView).text = databaseManager[position].expression
//            itemLayout.findViewById<TextView>(R.id.dateTextView).text = databaseManager[position].getFormattedDate()
//
//
//            val isDateVisible = databaseManager.isFirstUniqueFormattedDate(position)
//            itemLayout.findViewById<ViewGroup>(R.id.dateLayout).visibility = if (isDateVisible) View.VISIBLE else View.GONE
//
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return if (databaseManager.isEmpty()) {
//            1
//        } else {
//            databaseManager.size
//        }
//    }
//}
//
