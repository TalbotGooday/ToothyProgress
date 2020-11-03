package com.goodayapps.toothyprogress

import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_test.*


class TestActivity : AppCompatActivity(R.layout.activity_test), DemoPlayer.Listener {
	private val demoPlayer by lazy { DemoPlayer(this) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportActionBar?.setDisplayHomeAsUpEnabled(true);
		supportActionBar?.setDisplayShowHomeEnabled(true);

		again.setOnClickListener {
			toothyProgressBuilder.setFractureDataPairs(listOf(
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

		add.setOnClickListener {
			toothyProgressBuilder.newApex()
		}

		uploadToDemo.setOnClickListener {
			val fractureData = toothyProgressBuilder.getFractureData()
			printFractureData(fractureData)
			toothyProgressDemo.setFractureData(fractureData)
		}

		toothyProgressDemo.setListener(demoPlayer.progressListener)

		play.setOnClickListener { demoPlayer.playSomething() }

	}

	private fun printFractureData(fractureData: MutableList<PointF>) {
		val data = StringBuilder("\n")
		data.append(".setFractureDataPairs(listOf(\n")
		fractureData.forEach {
			data.append("${it.x}f to ${it.y}f,")
			data.append("\n")
		}
		data.append("))\n")

		Log.i("FractureData", data.toString())
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			android.R.id.home -> {
				finish()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun updateTimerAndSeekbar(duration: Long, currentPosition: Long) {
		// Updating progress bar
		val progress = currentPosition / duration.toFloat()
		toothyProgressDemo.setProgress(progress, false)
	}

	override fun onCompletion() {
		play.setImageResource(R.drawable.ic_play_arrow)
	}

	override fun onPlayerPause() {
		play.setImageResource(R.drawable.ic_play_arrow)
	}

	override fun onPlayerResume() {
		play.setImageResource(R.drawable.ic_pause)
	}
}