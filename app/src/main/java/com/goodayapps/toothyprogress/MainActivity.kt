package com.goodayapps.toothyprogress

import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.bosphere.verticalslider.VerticalSlider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {
	private val mp = MediaPlayer()
	private val mHandler = Handler()
	private val mUpdateTimeTask = object : Runnable{
		override fun run() {
			updateTimerAndSeekbar()

			// Running this thread after 10 milliseconds
			if (mp.isPlaying) {
				mHandler.postDelayed(this, 100)
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		again.setOnClickListener { toothyProgress.fracturesCount = 10; initProgressViews() }

		initProgressViews()

		testSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
			}

			override fun onStartTrackingTouch(seekBar: SeekBar) {
			}

			override fun onStopTrackingTouch(seekBar: SeekBar) {
				toothyProgress.setProgress(seekBar.progress / 100f)
			}
		})

		play.setOnClickListener {
			playSomething()
		}

		Handler().postDelayed({
			toothyProgress.setFractureData(listOf(
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
		}, 200)

		mp.setOnCompletionListener { // Changing button image to play button
			play.setImageResource(R.drawable.ic_play_arrow)
		}

		try {
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
			val afd = assets.openFd("short_music.mp3")
			mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
			afd.close()
			mp.prepare()
		} catch (e: Exception) {
			Toast.makeText(this, "Cannot load audio file", Toast.LENGTH_SHORT).show()
		}
	}

	private fun playSomething() {
		if (mp.isPlaying) {
			mp.pause()
			// Changing button image to play button
			play.setImageResource(R.drawable.ic_play_arrow)
		} else {
			// Resume song
			mp.start()
			// Changing button image to pause button
			play.setImageResource(R.drawable.ic_pause)
			// Updating progress bar
			mHandler.post(mUpdateTimeTask)
		}
	}

	private fun updateTimerAndSeekbar() {
		val totalDuration = mp.duration.toLong()
		val currentDuration = mp.currentPosition.toLong()

		// Updating progress bar
		val progress = getProgressSeekBar(currentDuration, totalDuration)
		toothyProgress.progressPercent = progress
	}

	private fun getProgressSeekBar(currentDuration: Long, totalDuration: Long): Float {
		return currentDuration / totalDuration.toFloat()
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