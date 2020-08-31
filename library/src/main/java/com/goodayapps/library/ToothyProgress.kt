package com.goodayapps.library

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import com.goodayapps.library.utils.convertDpToPixel
import kotlin.math.abs
import kotlin.random.Random.Default.nextFloat
import kotlin.random.Random.Default.nextInt

class ToothyProgress : View {

	private val progressPaint: Paint = Paint().apply {
		strokeCap = Paint.Cap.ROUND
		strokeWidth = context.convertDpToPixel(3).toFloat()
		style = Paint.Style.FILL_AND_STROKE
		color = Color.parseColor("#ffffff")
		isAntiAlias = true
	}

	private val progressBackgroundPaint: Paint = Paint().apply {
		strokeCap = Paint.Cap.ROUND
		strokeWidth = context.convertDpToPixel(3).toFloat()
		style = Paint.Style.FILL_AND_STROKE
		color = Color.parseColor("#959595")
		isAntiAlias = true
	}

	var progressPercent: Float = .0f
		set(value) {
			field = value
			invalidate()
		}

	var fracturesCount = nextInt(3, 7)
		set(value) {
			field = value
			getStepsData()
			invalidate()
		}

	private val canvasWidth
		get() = width - paddingStart - paddingEnd

	private val canvasHeight
		get() = height - paddingTop - paddingBottom

	private val data: MutableList<Pair<Float, Float>> = mutableListOf()

	private var progressAnimator: ValueAnimator? = null

	constructor(context: Context?) : super(context)
	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

	init {
		val padding = context.convertDpToPixel(12)
		setPadding(padding, padding, padding, padding)
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
		drawProgressForeground(canvas, progressPaint, progressPercent)
	}

	fun setProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float, animated: Boolean = true) {
		if (animated) {
			progressAnimator?.cancel()
			val currentProgress = progressPercent
			progressAnimator = ValueAnimator.ofFloat(currentProgress, progress).apply {
				duration = 220
				addUpdateListener { progressPercent = it.animatedValue as Float }
				start()
			}
		} else {
			progressPercent = progress
		}
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

	private fun getStepsData() {
		data.clear()

		val fracturesCountFl = fracturesCount.toFloat()

		val stepX = canvasWidth / fracturesCountFl
		val stepY = canvasHeight / fracturesCountFl

		val middleY = canvasHeight / 2f
		var startX = 0f
		var startY = middleY

		data.add(startX to startY)

		repeat(fracturesCount) {
			val direction = (if (it % 2 == 0) -1 else 1) * (nextFloat() * (2f - .1f) + .1f)

			val nextY = (middleY + stepY * direction)

			val factorX = nextFloat() * (1.5f - .3f) + .3f
			val nextX = startX + (stepX * factorX)

			if (fracturesCount == it + 1) {
				data.add(canvasWidth.toFloat() to nextY)
			} else {
				data.add(nextX to nextY)
			}

			startX = nextX
			startY = nextY
		}
	}

	private fun getStepsDataByFracture(data: List<Pair<Float, Float>>): List<Pair<Float, Float>> {
		val halfHeight = this.canvasHeight / 2
		val size = data.size
		val stepX = canvasWidth / size

		var prevX = paddingStart.toFloat()

		return data.mapIndexed { index, value ->
			val x = when (index) {
				size - 1 -> {
					canvasWidth.toFloat()
				}
				0 -> {
					.0f
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
		if (data.isEmpty()) return

		canvas.save()

		canvas.translate(paddingStart.toFloat(), paddingTop.toFloat())

		val first = data.first()

		var startX = first.first
		var startY = first.second
		val maxValue = progress * canvasWidth

		for (i in 1 until data.size) {
			val point = data[i]

			val nextX = point.first.coerceAtMost(maxValue)
			val nextY = getCoordinateY(startX to startY, point, maxValue)

			canvas.drawLine(startX, startY, nextX, nextY, paint)

			startX = nextX
			startY = nextY

			if (point.first > maxValue) break
		}

		canvas.restore()
	}

	private fun getCoordinateY(start: Pair<Float, Float>, end: Pair<Float, Float>, maxX: Float): Float {
		if (maxX >= end.first) return end.second

		val lambda = abs((start.first - maxX) / (maxX - end.first))

		return (start.second + end.second * lambda) / (1 + lambda)
	}
}