package com.goodayapps.toothyprogress

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.goodayapps.toothyprogress.databinding.ActivityMainBinding
import com.goodayapps.widget.ToothyProgress

class MainActivity : AppCompatActivity(), DemoPlayer.Listener {
    private val demoPlayer by lazy { DemoPlayer(this) }
    private var isPlayClickedBefore = false
    private var isFakeLoadingRun = false

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

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
                decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }


        with(binding) {
            toothyProgress.setListener(demoPlayer.progressListener)

            play.setOnClickListener { handlePlayClick() }

            actionTest.setOnClickListener { openTestScreen() }
        }
    }

    private fun changeType() = with(binding) {
        val currentType = toothyProgress.type
        if (currentType == ToothyProgress.Type.DETERMINATE) {
            toothyProgress.type = ToothyProgress.Type.INDETERMINATE
        } else {
            toothyProgress.type = ToothyProgress.Type.DETERMINATE
        }
    }

    private fun handlePlayClick() {
        if (isFakeLoadingRun) return

        if (isPlayClickedBefore) {
            demoPlayer.playSomething()
            isPlayClickedBefore = false
        } else {
            emulateLoading()
        }
    }

    private fun emulateLoading() = with(binding){
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
        binding.play.setImageResource(R.drawable.ic_play_arrow)
    }

    override fun onPlayerPause() {
        binding.play.setImageResource(R.drawable.ic_play_arrow)
    }

    override fun onPlayerResume() {
        binding.play.setImageResource(R.drawable.ic_pause)
    }

    override fun updateTimerAndSeekbar(duration: Long, currentPosition: Long) {
        // Updating progress bar
        val progress = currentPosition / duration.toFloat()
        binding.toothyProgress.setProgress(progress, false)
    }
}