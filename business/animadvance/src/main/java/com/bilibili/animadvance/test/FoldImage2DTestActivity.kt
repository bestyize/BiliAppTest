package com.bilibili.animadvance.test

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.drawToBitmap
import com.bilibili.animadvance.databinding.ActivityFoldImage2dTestBinding
import com.bilibili.animadvance.foldimage.FoldImage2DAnimationView
import com.bilibili.appresources.R

class FoldImage2DTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoldImage2dTestBinding
    private var animView: FoldImage2DAnimationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFoldImage2dTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.play.setOnClickListener {
            displayFoldImage()
        }
    }

    private fun displayFoldImage() {
        binding.bg.visibility = View.VISIBLE
        if (animView != null) {
            binding.root.removeView(animView)
        }

        animView =
            FoldImage2DAnimationView(this).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
            }
        val animView = animView ?: return

        binding.root.addView(animView, 0)

        animView.doOnPreDraw {
            val cover = binding.bg.drawToBitmap()
            val shape =
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.labubu_shape,
                )
            animView.startAnimation(
                coverBm = cover,
                brandBm = shape,
                params =
                    FoldImage2DAnimationView.AnimParams(
                        foldDuration = 2000,
                        verticalOffset = 400,
                        containerWidth = it.width.toFloat(),
                        containerHeight = it.height.toFloat(),
                    ),
                onFirstFrameRender = {
                    binding.bg.visibility = View.GONE
                },
                onEnd = {
                },
            )
        }
    }
}
