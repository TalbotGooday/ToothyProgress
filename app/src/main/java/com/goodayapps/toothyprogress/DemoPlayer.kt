package com.goodayapps.toothyprogress

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.goodayapps.widget.ToothyProgress

class DemoPlayer(private val context: Context) {
	private val mp = MediaPlayer()
	private val mHandler = Handler(Looper.getMainLooper())
	private val mUpdateTimeTask = object : Runnable {
		override fun run() {
			listener.updateTimerAndSeekbar(mp.duration.toLong(), mp.currentPosition.toLong())

			// Running this thread after 10 milliseconds
			if (mp.isPlaying) {
				mHandler.postDelayed(this, 100)
			}
		}
	}
	private val listener: Listener
		get() = context as Listener

	val progressListener = getToothyProgressListener()

	private fun getToothyProgressListener(): ToothyProgress.Listener {
		return object : ToothyProgress.Listener {
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
				mHandler.post(mUpdateTimeTask)
			}
		}
	}

	init {
		mp.setOnCompletionListener { // Changing button image to play button
			listener.onCompletion()
		}

		try {
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
			val afd = context.assets.openFd("sample.mp3")
			mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
			afd.close()
			mp.prepare()
		} catch (e: Exception) {
			Toast.makeText(context, "Cannot load audio file", Toast.LENGTH_SHORT).show()
		}
	}

	fun playSomething() {
		if (mp.isPlaying) {
			mp.pause()
			// Changing button image to play button
			listener.onPlayerPause()
		} else {
			// Resume song
			mp.start()
			// Changing button image to pause button
			listener.onPlayerResume()
			// Updating progress bar
			mHandler.post(mUpdateTimeTask)
		}
	}

	interface Listener {
		fun updateTimerAndSeekbar(duration: Long, currentPosition: Long)
		fun onCompletion()
		fun onPlayerPause()
		fun onPlayerResume()
	}
}