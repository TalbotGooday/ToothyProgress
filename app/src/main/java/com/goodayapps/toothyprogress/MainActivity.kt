package com.goodayapps.toothyprogress

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.goodayapps.widget.ToothyProgress
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main), DemoPlayer.Listener {
    private val demoPlayer by lazy { DemoPlayer(this) }
    private var isPlayClickedBefore = false
    private var isFakeLoadingRun = false

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


        toothyProgress.setListener(demoPlayer.progressListener)

        play.setOnClickListener { handlePlayClick() }

        actionTest.setOnClickListener { openTestScreen() }
    }

    private fun handlePlayClick() {
        if (isFakeLoadingRun) return

        if (isPlayClickedBefore) {
            demoPlayer.playSomething()
        } else {
            emulateLoading()
        }
    }

    private fun emulateLoading() {
        toothyProgress.type = ToothyProgress.Type.INDETERMINATE
        play.setImageResource(R.drawable.ic_download)
        isFakeLoadingRun = true

        Handler(Looper.getMainLooper()).postDelayed({
            demoPlayer.playSomething()
            isPlayClickedBefore = true
            isFakeLoadingRun = false
        }, 5000)
    }

    private fun openTestScreen() {
        startActivity(Intent(this, TestActivity::class.java))
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

    override fun updateTimerAndSeekbar(duration: Long, currentPosition: Long) {
        // Updating progress bar
        val progress = currentPosition / duration.toFloat()
        toothyProgress.setProgress(progress, false)
    }
}