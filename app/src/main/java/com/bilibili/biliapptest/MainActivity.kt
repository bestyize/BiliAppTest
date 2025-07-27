package com.bilibili.biliapptest

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.doOnPreDraw
import androidx.core.view.drawToBitmap
import com.bilibili.animadvance.unveil.UnveilView
import com.bilibili.biliapptest.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private var unveilView: UnveilView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.play.setOnClickListener {
            displayUnveil()
        }

    }


    private fun displayUnveil() {
        if (unveilView != null) {
            binding.root.removeView(unveilView)
        }
        unveilView = UnveilView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            tag = "unveilView"
        }
        val unveilView = unveilView ?: return
        binding.root.addView(unveilView)
        unveilView.doOnPreDraw {
            val cover = binding.bg.drawToBitmap()
            unveilView.startAnimation(
                bm = cover, anim = UnveilView.UnveilAnimParams(
                    edgeStart = 1f,
                    edgeEnd = 0f,
                    edgeDuration = 1000,
                    peakStart = 0.9f,
                    peakEnd = -0.5f,
                    peakDuration = 800,
                    containerWidth = it.width.toFloat(),
                    containerHeight = it.height.toFloat(),
                    cx = it.width / 2f
                ), onEnd = {

                }, onFirstFrameRender = {
                    binding.bg.visibility = View.GONE
                })
        }
    }
}