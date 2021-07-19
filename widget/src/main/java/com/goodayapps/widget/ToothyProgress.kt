package com.goodayapps.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.RangeInfo
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import com.goodayapps.widget.utils.dp
import org.jetbrains.annotations.NotNull
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random.Default.nextFloat

class ToothyProgress : View {

    private val debugPaint: Paint = getDebugPaint()
    private val trackPaint: Paint = getMarkerPaint()
    private val progressPaint: Paint = getProgressPaint()
    private val progressBackgroundPaint: Paint = getProgressBackgroundPaint()

    private var indeterminateStart = .0f
    private var indeterminateEnd = .3f
    private val indeterminateAnimator by lazy { createIndeterminateAnimator() }

    var isBuilderMode = false
    var indeterminateAnimationDuration = 2000L
    var indeterminateTrackSize = .1f
        set(value) {
            field = value.coerceIn(.1f, .7f)
        }

    private var progress: Float = .0f
        set(value) {
            field = value
            listener?.onProgressChanged(value, isTouching)
            invalidate()
        }

    var type = Type.DETERMINATE
        set(value) {
            changeType(value)

            field = value
        }

    private fun changeType(value: Type) {
        if (value == type) return

        if (value == Type.DETERMINATE) {
            invalidate()
        } else {
            indeterminateAnimator.start()
        }
    }

    var progressStrokeCap = Paint.Cap.ROUND
        set(value) {
            field = value
            progressPaint.strokeCap = value

            invalidate()
        }

    var progressBackgroundStrokeCap = Paint.Cap.ROUND
        set(value) {
            field = value
            progressBackgroundPaint.strokeCap = value

            invalidate()
        }

    var strokeLineCapTrack = Paint.Cap.ROUND
        set(value) {
            field = value
            trackPaint.strokeCap = value

            invalidate()
        }

    @ColorInt
    var progressColor = Color.parseColor("#ffffff")
        set(value) {
            field = value
            progressPaint.color = value

            invalidate()
        }

    @ColorInt
    var progressBackgroundColor = Color.parseColor("#959595")
        set(value) {
            field = value
            progressBackgroundPaint.color = value

            invalidate()
        }

    @ColorInt
    var trackColor = Color.parseColor("#959595")
        set(value) {
            field = value
            trackPaint.color = value

            invalidate()
        }

    @Dimension(unit = Dimension.PX)
    var progressWidth = context.dp(3).toFloat()
        set(value) {
            field = value
            progressPaint.strokeWidth = value

            invalidate()
        }

    @Dimension(unit = Dimension.PX)
    var trackWidth = context.dp(3).toFloat()
        set(value) {
            field = value
            trackPaint.strokeWidth = value

            invalidate()
        }

    @Dimension(unit = Dimension.PX)
    var progressBackgroundWidth = context.dp(3).toFloat()
        set(value) {
            field = value
            progressBackgroundPaint.strokeWidth = value

            invalidate()
        }

    private val canvasWidth
        get() = width - paddingStart - paddingEnd

    private val canvasHeight
        get() = height - paddingTop - paddingBottom

    private val canvasHalfHeight
        get() = canvasHeight / 2f

    private val data: MutableList<PointF> = mutableListOf()
    private val fractureData: MutableList<PointF> = mutableListOf()

    private var progressAnimator: ValueAnimator? = null

    private var pointerPosition = -1f
    private var isTouching = false

    private var listener: Listener? = null

    private val nearestApex
        get() = data.getOrNull(nearestApexIndex)

    private var nearestApexIndex: Int = -1

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        inflateAttrs(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        inflateAttrs(attrs)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        inflateAttrs(attrs)
    }

    init {
        val padding = context.dp(12)
        setPadding(padding, padding, padding, padding)
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = "android.widget.ProgressBar"
        info.contentDescription = "ProgressBar"

        val rangeInfo = RangeInfo.obtain(
            RangeInfo.RANGE_TYPE_INT,
            0.0f,
            1f,
            progress
        )

        info.rangeInfo = rangeInfo
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setFractureDataPairs(
            listOf(
                .5f to .5f,
                .5f to 0f,
                .5f to .5f,
                1f to -.5f,
                1f to .5f,
                .5f to .0f,
                1f to 1f,
                1f to .0f,
                1f to .0f
            )
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //Draw background
        drawProgress(canvas, progressBackgroundPaint)

        if (type == Type.DETERMINATE) {
            if (isBuilderMode.not()) {
                //Draw foreground
                drawProgress(canvas, progressPaint, progress)
                //Draw pointer
                drawPointer(canvas)
            }
        } else {
            drawIndeterminateProgress(canvas, progressPaint, indeterminateStart, indeterminateEnd)
        }

        //Draw debug
        builderDrawDebug(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            -> {
                if (type == Type.DETERMINATE) {
                    if (nearestApex == null) {
                        trackTouch(event)
                    } else {
                        builderMoveApex(event)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (type == Type.DETERMINATE) {
                    stopTrackingTouch()
                }
            }

            MotionEvent.ACTION_CANCEL,
            -> {
                if (type == Type.DETERMINATE) {
                    stopTrackingTouch()
                }
            }
        }

        return type == Type.DETERMINATE
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return superState?.let {
            val savedState = SavedState(it)
            savedState.progress = progress
            savedState.type = type.value
            savedState
        } ?: superState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        this.progress = state.progress
        this.type = Type.from(state.type)
    }

    fun setProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float, animated: Boolean = true) {
        if (isTouching) return

        if (type != Type.DETERMINATE) {
            type = Type.DETERMINATE
        }

        if (animated) {
            progressAnimator?.cancel()
            val currentProgress = this.progress
            progressAnimator = ValueAnimator.ofFloat(currentProgress, progress).apply {
                duration = 220
                addUpdateListener { this@ToothyProgress.progress = it.animatedValue as Float }
                start()
            }
        } else {
            this.progress = progress
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun setFractureDataPairs(data: List<Pair<Float, Float>>) {
        setFractureData(data.map { PointF(it.first, it.second) })
    }

    fun setFractureData(data: List<PointF>) {
        this.data.clear()
        this.fractureData.clear()
        this.data.addAll(getStepsDataByFracture(data))
        this.fractureData.addAll(data)

        invalidate()
    }

    fun getFractureData() = fractureData

    private fun stopTrackingTouch() {
        isTouching = false
        pointerPosition = -1f
        nearestApexIndex = -1

        invalidate()

        listener?.onStopTrackingTouch(progress)
    }

    private fun trackTouch(event: MotionEvent) {
        if (isTouching.not()) {
            listener?.onStartTrackingTouch(progress)
        }

        isTouching = true
        pointerPosition = when {
            event.x >= (canvasWidth + paddingEnd) -> canvasWidth.toFloat() + paddingEnd
            event.x <= paddingStart -> paddingStart.toFloat()
            else -> event.x
        }

        progress = ((pointerPosition - paddingStart) / canvasWidth.toFloat()).coerceAtMost(1f)

        if (isBuilderMode) {
            builderFindNearestApexForPointer(pointerPosition, event.y)
        }
    }

    private fun getStepsDataByFracture(data: List<PointF>): List<PointF> {
        val size = data.size
        val stepX = canvasWidth / size

        var prevX = paddingStart.toFloat()

        val lastIndex = size - 1

        return data.mapIndexed { index, value ->
            val x = when (index) {
                0 -> {
                    .0f
                }
                lastIndex -> {
                    canvasWidth.toFloat()
                }
                else -> {
                    (stepX * value.x) + prevX
                }
            }

            prevX = x

            val y = canvasHalfHeight + (value.y * canvasHalfHeight)

            PointF(x, y)
        }
    }

    private fun drawPointer(canvas: Canvas) {
        if (pointerPosition < 0f) return
        if (isBuilderMode) return

        canvas.save()
        canvas.translate(0f, paddingTop.toFloat())

        canvas.drawLine(pointerPosition, 0f, pointerPosition, canvasHeight.toFloat(), trackPaint)

        canvas.restore()
    }

    private fun drawProgress(canvas: Canvas, paint: Paint, progress: Float = 1f) {
        if (data.isEmpty() || progress == 0f) return

        canvas.save()

        canvas.translate(paddingStart.toFloat(), paddingTop.toFloat())

        val first = data.first()

        var startX = first.x
        var startY = first.y
        val maxValue = progress * canvasWidth

        val path = Path()
        path.moveTo(startX, startY)

        for (nextIndex in 1 until data.size) {
            val point = data[nextIndex]

            val nextX = point.x.coerceAtMost(maxValue)
            val nextY = getCoordinateYForMax(PointF(startX, startY), point, maxValue)

            path.lineTo(nextX, nextY)

            startX = nextX
            startY = nextY

            if (point.x > maxValue) break
        }

        canvas.drawPath(path, paint)

        canvas.restore()
    }

    private fun drawIndeterminateProgress(
        canvas: Canvas,
        paint: Paint,
        lower: Float,
        upper: Float
    ) {
        canvas.save()

        canvas.translate(paddingStart.toFloat(), paddingTop.toFloat())

        val minValue = lower * canvasWidth
        val maxValue = upper * canvasWidth

        val nextValidIndex = data.indexOfFirst { it.x > minValue }.coerceAtLeast(0)
        val prevIndex = (nextValidIndex - 1).coerceAtLeast(0)
        val first = data[prevIndex]
        val second = data[nextValidIndex] //Can cause the ArrayIndexOutOfBoundsException

        var startX = minValue
        var startY = getCoordinateYForMin(first, second, minValue)

        val path = Path()
        path.moveTo(startX, startY)

        for (nextIndex in nextValidIndex until data.size) {
            val point = data[nextIndex]

            val nextX = point.x.coerceAtMost(maxValue)
            val nextY = getCoordinateYForMax(PointF(startX, startY), point, maxValue)

            path.lineTo(nextX, nextY)

            startX = nextX
            startY = nextY

            if (point.x > maxValue) break
        }

        canvas.drawPath(path, paint)

        canvas.restore()
    }

    private fun getCoordinateYForMax(start: PointF, end: PointF, maxX: Float): Float {
        if (maxX >= end.x) return end.y

        val lambda = abs((start.x - maxX) / (maxX - end.x))

        return (start.y + end.y * lambda) / (1 + lambda)
    }

    private fun getCoordinateYForMin(start: PointF, end: PointF, minX: Float): Float {
        val lambda = abs((minX - start.x) / (end.x - minX))

        return (start.y + end.y * lambda) / (1 + lambda)
    }

    //region Builder
    private fun builderMoveApex(event: MotionEvent) {
        val apex = nearestApex ?: return
        val fracture = fractureData.getOrNull(nearestApexIndex) ?: return

        val prevApex = data.getOrNull(nearestApexIndex - 1)
        val nextApex = data.getOrNull(nearestApexIndex + 1)

        apex.apply {
            this.x = event.x.coerceIn(
                prevApex?.x ?: paddingStart.toFloat(),
                nextApex?.x ?: canvasWidth.toFloat()
            )
            this.y = event.y.coerceIn(0f, canvasHeight.toFloat())
        }

        var stepX: Float
        val prevX: Float

        when {
            prevApex != null -> {
                stepX = apex.x - prevApex.x
                prevX = prevApex.x
            }
            nextApex != null -> {
                stepX = nextApex.x - apex.x
                prevX = 1f
            }
            else -> {
                stepX = 1f
                prevX = 0f
            }
        }

        if (stepX == 0f) stepX = 1f

        fracture.apply {
            this.y = (apex.y - canvasHalfHeight) / canvasHalfHeight
            this.x = if (stepX != 0f) {
                (apex.x - prevX) / stepX
            } else {
                0f
            }
        }

        data[nearestApexIndex] = apex
        fractureData[nearestApexIndex] = fracture

        postInvalidate()
    }

    private fun builderDrawDebug(canvas: Canvas) {
        if (isBuilderMode.not()) return

        canvas.save()

        canvas.translate(paddingStart.toFloat(), paddingTop.toFloat())

        debugPaint.style = Paint.Style.STROKE
        canvas.drawRect(Rect(0, 0, canvasWidth, canvasHeight), debugPaint)

        val apex = nearestApex
        if (apex != null) {
            debugPaint.style = Paint.Style.FILL
            canvas.drawCircle(apex.x, apex.y, context.dp(6).toFloat(), debugPaint)
            canvas.drawLine(0f, apex.y, canvasWidth.toFloat(), apex.y, debugPaint)
            canvas.drawLine(apex.x, 0f, apex.x, canvasHeight.toFloat(), debugPaint)
        } else {
            debugPaint.style = Paint.Style.FILL
            canvas.drawLine(
                0f,
                canvasHalfHeight,
                canvasWidth.toFloat(),
                canvasHalfHeight,
                debugPaint
            )

            for (nextIndex in 1 until data.size) {
                val point = data[nextIndex]
                canvas.drawLine(point.x, 0f, point.x, canvasHeight.toFloat(), debugPaint)
            }
        }

        canvas.restore()
    }

    private fun builderFindNearestApexForPointer(pointerX: Float, pointerY: Float) {
        var lastL: Float = Float.MAX_VALUE

        val closestRange = canvasWidth / data.size.toFloat()

        for (i in 0 until data.size) {
            val apex = data[i]

            if (apex.x !in pointerX - closestRange..pointerX + closestRange) continue

            val coordV = PointF(pointerX - apex.x, pointerY - apex.y)

            val l = abs(sqrt(coordV.x * coordV.x + coordV.y * coordV.y))

            if (l < lastL) {
                nearestApexIndex = i
                lastL = l
            }
        }
    }

    //endregion

    //region Attributes
    private fun inflateAttrs(attrs: AttributeSet?) {
        val resAttrs = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ToothyProgress,
            0,
            0
        )

        with(resAttrs) {
            progressStrokeCap =
                getCapType(getInt(R.styleable.ToothyProgress_strokeLineCapProgress, 1))
            progressBackgroundStrokeCap =
                getCapType(getInt(R.styleable.ToothyProgress_strokeLineCapProgressBackground, 1))
            strokeLineCapTrack =
                getCapType(getInt(R.styleable.ToothyProgress_strokeLineCapTrack, 1))

            progressColor = getColor(R.styleable.ToothyProgress_progressColor, progressColor)
            progressBackgroundColor = getColor(
                R.styleable.ToothyProgress_progressBackgroundColor,
                progressBackgroundColor
            )
            trackColor = getColor(R.styleable.ToothyProgress_trackColor, trackColor)

            progressWidth = getDimension(R.styleable.ToothyProgress_progressWidth, progressWidth)
            trackWidth = getDimension(R.styleable.ToothyProgress_progressWidth, trackWidth)
            progressBackgroundWidth = getDimension(
                R.styleable.ToothyProgress_progressBackgroundWidth,
                progressBackgroundWidth
            )
            type = Type.from(getInt(R.styleable.ToothyProgress_progressType, type.value))

            progress = getFloat(R.styleable.ToothyProgress_progress, progress).coerceIn(0f, 1f)
            indeterminateTrackSize =
                getFloat(R.styleable.ToothyProgress_indeterminateTrackSize, indeterminateTrackSize)
            isBuilderMode = getBoolean(R.styleable.ToothyProgress_isBuilderMode, false)

            recycle()
        }
    }

    private fun getCapType(strokeLineCap: Int): Paint.Cap {
        return when (strokeLineCap) {
            0 -> Paint.Cap.BUTT
            1 -> Paint.Cap.ROUND
            else -> Paint.Cap.SQUARE
        }
    }
    //endregion

    //region Paint
    private fun getDebugPaint(): Paint {
        return Paint().apply {
            strokeCap = Paint.Cap.ROUND
            strokeWidth = context.dp(1).toFloat()
            style = Paint.Style.FILL
            color = Color.MAGENTA
            isAntiAlias = true
        }
    }

    private fun getMarkerPaint(): Paint {
        return Paint().apply {
            strokeCap = Paint.Cap.ROUND
            strokeWidth = context.dp(3).toFloat()
            style = Paint.Style.FILL
            color = trackColor
            isAntiAlias = true
        }
    }

    private fun getProgressPaint(): Paint {
        return Paint().apply {
            strokeCap = Paint.Cap.ROUND
            strokeWidth = context.dp(3).toFloat()
            style = Paint.Style.STROKE
            color = progressColor
            isAntiAlias = true
        }
    }

    private fun getProgressBackgroundPaint(): Paint {
        return Paint().apply {
            strokeCap = Paint.Cap.ROUND
            strokeWidth = context.dp(3).toFloat()
            style = Paint.Style.STROKE
            color = progressBackgroundColor
            isAntiAlias = true
        }
    }

    fun newApex(position: Int = data.size) {
        if ((position in 0..data.size).not()) return

        val nextY = nextFloat() * 2f - 1f
        this.fractureData.add(position, PointF(1 - nextY, nextY))

        setFractureData(ArrayList(fractureData))
    }
    //endregion

    private fun createIndeterminateAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(0f, 1f - indeterminateTrackSize)

        animator.addUpdateListener {
            val value = it.animatedValue as Float

            indeterminateStart = value
            indeterminateEnd = indeterminateStart + indeterminateTrackSize

            postInvalidate()
        }

        animator.duration = indeterminateAnimationDuration
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE

        return animator
    }

    enum class Type(val value: Int) {
        DETERMINATE(0),
        INDETERMINATE(1);

        companion object {
            fun from(value: Int) = values().firstOrNull { it.value == value } ?: DETERMINATE
        }
    }

    class SavedState : BaseSavedState {
        var progress: Float = 0f
        var type: Int = Type.DETERMINATE.value

        constructor(superState: Parcelable) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            this.progress = `in`.readFloat()
            this.type = `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(this.progress)
            out.writeInt(this.type)
        }

        companion object {
            @JvmField
            @NotNull
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    interface Listener {
        fun onProgressChanged(progress: Float, fromUser: Boolean) {}
        fun onStartTrackingTouch(progress: Float) {}
        fun onStopTrackingTouch(progress: Float) {}
    }
}