package com.bilibili.animadvance.test

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.drawToBitmap
import com.bilibili.animadvance.databinding.ActivityUnverlTestBinding
import com.bilibili.animadvance.unveil.UnveilView

class UnveilTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnverlTestBinding

    private var unveilView: UnveilView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityUnverlTestBinding.inflate(layoutInflater)
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
            val brand = BitmapFactory.decodeResource(
                resources,
                com.bilibili.appresources.R.drawable.labubu_shape
            )
            unveilView.startAnimation(
                coverBm = cover,
                params = UnveilView.AnimParams(
                    edgeStart = 1f,
                    edgeEnd = 0f,
                    edgeDuration = 1000,
                    peakStart = 0.9f,
                    peakEnd = -0.5f,
                    peakDuration = 800,
                    containerWidth = it.width.toFloat(),
                    containerHeight = it.height.toFloat(),
                    brandStart = 1f,
                    brandEnd = 0.2f,
                    brandWidth = 400,
                    brandHeight = 200,
                    cx = it.width / 2f
                ),
                brandBm = brand,
                onEnd = {

                }, onFirstFrameRender = {
                    binding.bg.visibility = View.GONE
                })
        }
    }
}