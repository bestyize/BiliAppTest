package com.bilibili.animadvance.test

import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.drawToBitmap
import com.bilibili.animadvance.databinding.ActivityShapeCutTestBinding
import com.bilibili.animadvance.shapecut.ShapeCutAnimationView

class ShapeCutTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShapeCutTestBinding

    private var animView: ShapeCutAnimationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityShapeCutTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.play.setOnClickListener {
            displayShapeCut()
        }
    }

    private fun displayShapeCut() {
        binding.bg.visibility = View.VISIBLE
        if (animView != null) {
            binding.root.removeView(animView)
        }
        animView = ShapeCutAnimationView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val animView = animView ?: return

        binding.root.addView(animView)

        animView.doOnPreDraw {
            val cover = binding.bg.drawToBitmap()
            val shape = BitmapFactory.decodeResource(
                resources,
                com.bilibili.appresources.R.drawable.labubu_shape
            )
            val targetRect = Rect(
                (it.width / 2) - 300,
                500,
                (it.width / 2) + 300,
                800
            )
            animView.startAnimation(
                coverBm = cover,
                shapeBm = shape,
                params = ShapeCutAnimationView.AnimParams(
                    targetRect = targetRect,
                    scaleInitPercent = 25f,
                    scaleEndPercent = 2f,
                    scaleDuration = 3000,
                    alphaDuration = 3000,
                    containerWidth = it.width.toFloat(),
                    containerHeight = it.height.toFloat()
                ),
                onFirstFrameRender = {
                    binding.bg.visibility = View.GONE
                }, onEnd = {

                }
            )
        }
    }
}