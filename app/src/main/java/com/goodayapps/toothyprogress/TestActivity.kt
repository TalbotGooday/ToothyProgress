package com.goodayapps.toothyprogress

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.bosphere.verticalslider.VerticalSlider
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity(R.layout.activity_test) {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		again.setOnClickListener { toothyProgress.fracturesCount = 10; initProgressViews() }

		testSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
			}

			override fun onStartTrackingTouch(seekBar: SeekBar) {
			}

			override fun onStopTrackingTouch(seekBar: SeekBar) {
				toothyProgress.setProgress(seekBar.progress / 100f)
			}
		})

		initProgressViews()

	}


	private fun initProgressViews() {
		fractureSeekBars.removeAllViews()

		for (i: Int in 0..toothyProgress.fracturesCount) {
			fractureSeekBars.addView(getProgressView(toothyProgress.getFractureY(i)))
		}
	}

	private fun getProgressView(fractureY: Float): View? {
		return LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT).apply {
				orientation = LinearLayout.VERTICAL
				gravity = Gravity.CENTER
			}
			addView(VerticalSlider(this.context).apply {
				layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT).apply {
					val margin = convertDpToPixel(10)
					setMargins(margin, 0, margin, 0)
					weight = 1f
				}

				setThumbColor(Color.WHITE)
				setTrackBgColor(Color.parseColor("#66FFFFFF"))
				setTrackFgColor(Color.parseColor("#A2A4A8"))

				setProgress(getProgressFromFracture(fractureY))

				setOnSliderProgressChangeListener {
					val index = fractureSeekBars.indexOfChild(this.parent as View)

					val height = getHeightInRangeFromProgress(it)

					toothyProgress.setFractureY(index, height)

					(parent as LinearLayout).findViewWithTag<AppCompatTextView>("label")?.text = height.toString()
				}
			})

			addView(AppCompatTextView(this.context).apply {
				tag = "label"
				setTextColor(Color.WHITE)
				text = fractureY.toString()
			})
		}
	}

	private fun getHeightInRangeFromProgress(progress: Float): Float {
		return ((2f * progress) - 1f) * -1f
	}

	private fun getProgressFromFracture(fracture: Float): Float {
		return (-fracture + 1f) / 2f
	}

}