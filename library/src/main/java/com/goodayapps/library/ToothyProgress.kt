package com.goodayapps.library

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.RangeInfo
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import com.goodayapps.library.utils.convertDpToPixel
import kotlin.math.abs
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ToothyProgress : View {

	private val trackPaint: Paint = getMarkerPaint()
	private val progressPaint: Paint = getProgressPaint()
	private val progressBackgroundPaint: Paint = getProgressBackgroundPaint()

	private var progress: Float = .0f
		set(value) {
			field = value
			listener?.onProgressChanged(value, isTouching)
			invalidate()
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
	var progressWidth = context.convertDpToPixel(3).toFloat()
		set(value) {
			field = value
			progressPaint.strokeWidth = value

			invalidate()
		}

	@Dimension(unit = Dimension.PX)
	var trackWidth = context.convertDpToPixel(3).toFloat()
		set(value) {
			field = value
			trackPaint.strokeWidth = value

			invalidate()
		}

	@Dimension(unit = Dimension.PX)
	var progressBackgroundWidth = context.convertDpToPixel(3).toFloat()
		set(value) {
			field = value
			progressBackgroundPaint.strokeWidth = value

			invalidate()
		}

	private val canvasWidth
		get() = width - paddingStart - paddingEnd

	private val canvasHeight
		get() = height - paddingTop - paddingBottom

	private val data: MutableList<Pair<Float, Float>> = mutableListOf()

	private var progressAnimator: ValueAnimator? = null

	private var pointerPosition = -1f
	private var isTouching = false

	private var listener: Listener? = null

	constructor(context: Context?) : super(context)
	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
		inflateAttrs(attrs)
	}

	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		inflateAttrs(attrs)
	}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
		inflateAttrs(attrs)
	}

	init {
		val padding = context.convertDpToPixel(12)
		setPadding(padding, padding, padding, padding)
	}


	override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
		super.onInitializeAccessibilityNodeInfo(info)
		info.className = "android.widget.SeekBar"
		info.contentDescription = "SeekBar"

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
		setFractureData(listOf(
				.5f to .5f,
				.5f to 0f,
				.5f to .5f,
				1f to -.5f,
				1f to .5f,
				.5f to .0f,
				1f to 1f,
				1f to .0f,
				1f to .0f
		))
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		drawProgressForeground(canvas, progressBackgroundPaint)
		drawProgressForeground(canvas, progressPaint, progress)
		drawPointer(canvas)
	}

	private fun drawPointer(canvas: Canvas) {
		if (pointerPosition < 0f) return

		canvas.save()
		canvas.translate(0f, paddingTop.toFloat())

		canvas.drawLine(pointerPosition, 0f, pointerPosition, canvasHeight.toFloat(), trackPaint)

		canvas.restore()
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		if (!isEnabled) {
			return false
		}

		when (event.actionMasked) {
			MotionEvent.ACTION_DOWN,
			MotionEvent.ACTION_MOVE,
			-> {
				trackTouch(event)

				return true
			}
			MotionEvent.ACTION_UP,
			MotionEvent.ACTION_CANCEL,
			-> {
				stopTrackingTouch()
				return true
			}
		}

		return super.onTouchEvent(event)
	}

	private fun stopTrackingTouch() {
		isTouching = false
		pointerPosition = -1f

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
	}

	fun setProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float, animated: Boolean = true) {
		if (isTouching) return

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

	fun setFractureData(data: List<Pair<Float, Float>>) {
		this.data.clear()
		this.data.addAll(getStepsDataByFracture(data))

		invalidate()
	}

	fun setFractureY(index: Int, @FloatRange(from = -1.0, to = 1.0) scale: Float) {
		var fracture = data.getOrNull(index) ?: return

		val halfHeight = this.canvasHeight / 2

		fracture = fracture.first to (halfHeight + (scale * halfHeight))

		data[index] = fracture

		invalidate()
	}

	fun getFractureY(index: Int): Float {
		val halfHeight = this.canvasHeight / 2
		val height = data.getOrNull(index)?.second ?: return 0f

		return (halfHeight - height) / height
	}
//
//	private fun getStepsData() {
//		data.clear()
//
//		val fracturesCountFl = fracturesCount.toFloat()
//
//		val stepX = canvasWidth / fracturesCountFl
//		val stepY = canvasHeight / fracturesCountFl
//
//		val middleY = canvasHeight / 2f
//		var startX = 0f
//		var startY = middleY
//
//		data.add(startX to startY)
//
//		repeat(fracturesCount) {
//			val direction = (if (it % 2 == 0) -1 else 1) * (nextFloat() * (2f - .1f) + .1f)
//
//			val nextY = (middleY + stepY * direction)
//
//			val factorX = nextFloat() * (1.5f - .3f) + .3f
//			val nextX = startX + (stepX * factorX)
//
//			if (fracturesCount == it + 1) {
//				data.add(canvasWidth.toFloat() to nextY)
//			} else {
//				data.add(nextX to nextY)
//			}
//
//			startX = nextX
//			startY = nextY
//		}
//	}

	private fun getStepsDataByFracture(data: List<Pair<Float, Float>>): List<Pair<Float, Float>> {
		val halfHeight = this.canvasHeight / 2
		val size = data.size
		val stepX = canvasWidth / size

		var prevX = paddingStart.toFloat()

		val firstIndex = 0
		val lastIndex = size - 1

		return data.mapIndexed { index, value ->
			val x = when (index) {
				firstIndex -> {
					.0f
				}
				lastIndex -> {
					canvasWidth.toFloat()
				}
				else -> {
					(stepX * value.first) + prevX
				}
			}

			prevX = x

			val y = halfHeight + (value.second * halfHeight)

			x to y
		}
	}

	private fun drawProgressForeground(canvas: Canvas, paint: Paint, progress: Float = 1f) {
		if (data.isEmpty() || progress == 0f) return

		canvas.save()

		canvas.translate(paddingStart.toFloat(), paddingTop.toFloat())

		val first = data.first()

		var startX = first.first
		var startY = first.second
		val maxValue = progress * canvasWidth

		val path = Path()
		path.moveTo(startX, startY)

		for (nextIndex in 1 until data.size) {
			val point = data[nextIndex]

			val nextX = point.first.coerceAtMost(maxValue)
			val nextY = getCoordinateY(startX to startY, point, maxValue)

			path.lineTo(nextX, nextY)

			startX = nextX
			startY = nextY

			if (point.first > maxValue) break
		}

		canvas.drawPath(path, paint)
		canvas.restore()
	}

	private fun getCoordinateY(start: Pair<Float, Float>, end: Pair<Float, Float>, maxX: Float): Float {
		if (maxX >= end.first) return end.second

		val lambda = abs((start.first - maxX) / (maxX - end.first))

		return (start.second + end.second * lambda) / (1 + lambda)
	}

	//region Attributes
	private fun inflateAttrs(attrs: AttributeSet?) {
		val resAttrs = context.theme.obtainStyledAttributes(
				attrs,
				R.styleable.ToothyProgress,
				0,
				0
		) ?: return

		with(resAttrs) {
			progressStrokeCap = getCapType(getInt(R.styleable.ToothyProgress_strokeLineCapProgress, 1))
			progressBackgroundStrokeCap = getCapType(getInt(R.styleable.ToothyProgress_strokeLineCapProgressBackground, 1))
			strokeLineCapTrack = getCapType(getInt(R.styleable.ToothyProgress_strokeLineCapTrack, 1))

			progressColor = getColor(R.styleable.ToothyProgress_progressColor, progressColor)
			progressBackgroundColor = getColor(R.styleable.ToothyProgress_progressBackgroundColor, progressBackgroundColor)
			trackColor = getColor(R.styleable.ToothyProgress_trackColor, trackColor)

			progressWidth = getDimension(R.styleable.ToothyProgress_progressWidth, progressWidth)
			trackWidth = getDimension(R.styleable.ToothyProgress_progressWidth, trackWidth)
			progressBackgroundWidth = getDimension(R.styleable.ToothyProgress_progressBackgroundWidth, progressBackgroundWidth)

			progress = getFloat(R.styleable.ToothyProgress_progress, progress).coerceIn(0f, 1f)

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
	private fun getMarkerPaint(): Paint {
		return Paint().apply {
			strokeCap = Paint.Cap.ROUND
			strokeWidth = context.convertDpToPixel(3).toFloat()
			style = Paint.Style.FILL
			color = trackColor
			isAntiAlias = true
		}
	}

	private fun getProgressPaint(): Paint {
		return Paint().apply {
			strokeCap = Paint.Cap.ROUND
			strokeWidth = context.convertDpToPixel(3).toFloat()
			style = Paint.Style.STROKE
			color = progressColor
			isAntiAlias = true
		}
	}

	private fun getProgressBackgroundPaint(): Paint {
		return Paint().apply {
			strokeCap = Paint.Cap.ROUND
			strokeWidth = context.convertDpToPixel(3).toFloat()
			style = Paint.Style.STROKE
			color = progressBackgroundColor
			isAntiAlias = true
		}
	}
	//endregion

	interface Listener {
		fun onProgressChanged(progress: Float, fromUser: Boolean) {}
		fun onStartTrackingTouch(progress: Float) {}
		fun onStopTrackingTouch(progress: Float) {}
	}
}