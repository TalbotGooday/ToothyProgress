package com.goodayapps.toothyprogress

import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goodayapps.library.ToothyProgress
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {
	private val mp = MediaPlayer()
	private val mHandler = Handler()
	private val mUpdateTimeTask = object : Runnable {
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
		window.apply {
			clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			}
			decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				statusBarColor = Color.TRANSPARENT
			}
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
				decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
			}
		}


		toothyProgress.setListener(object : ToothyProgress.Listener {
			override fun onProgressChanged(progress: Float, fromUser: Boolean) {
				Log.d("toothyProgress", "onProgressChanged: progress: $progress, fromUser: $fromUser")
			}

			override fun onStartTrackingTouch(progress: Float) {
				// remove message Handler from updating progress bar
				mHandler.removeCallbacks(mUpdateTimeTask)
			}

			override fun onStopTrackingTouch(progress: Float) {
				mHandler.removeCallbacks(mUpdateTimeTask)
				val totalDuration = mp.duration
				val currentPosition = (progress * totalDuration).toInt()

				// forward or backward to certain seconds
				mp.seekTo(currentPosition)

				// update timer progress again

				// update timer progress again
				mHandler.post(mUpdateTimeTask)
			}
		})

		play.setOnClickListener { playSomething() }

		actionTest.setOnClickListener { openTestScreen() }
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

	private fun openTestScreen() {
		startActivity(Intent(this, TestActivity::class.java))
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
		toothyProgress.setProgress(progress, false)
	}

	private fun getProgressSeekBar(currentDuration: Long, totalDuration: Long): Float {
		return currentDuration / totalDuration.toFloat()
	}

}