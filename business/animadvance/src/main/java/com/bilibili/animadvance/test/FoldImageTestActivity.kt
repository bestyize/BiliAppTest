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
import com.bilibili.animadvance.databinding.ActivityFoldImageTestBinding
import com.bilibili.animadvance.foldimage.FoldImageAnimationView
import com.bilibili.animadvance.shapecut.ShapeCutAnimationView

class FoldImageTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoldImageTestBinding
    private var animView: FoldImageAnimationView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFoldImageTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.play.setOnClickListener {
            displayFoldImage()
        }

        val originalBitmap = BitmapFactory.decodeResource(resources, com.bilibili.appresources.R.drawable.bizhi)

        // 设置图片
        binding.foldable.setBitmap(originalBitmap)

        // 设置折叠位置（0.0-1.0之间，0.5表示中间）
        binding.foldable.setFoldPosition(0.5f)

        // 设置折叠深度（影响3D效果）
        binding.foldable.setFoldDepth(30f)

        // 折叠到90度
        binding.foldable.foldToAngle(45f, 5500)
    }

    private fun displayFoldImage() {
        binding.bg.visibility = View.VISIBLE
        if (animView != null) {
            binding.root.removeView(animView)
        }

        animView = FoldImageAnimationView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val animView = animView ?: return

        binding.root.addView(animView, 0)

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
                brandBm = shape,
                params = FoldImageAnimationView.AnimParams(
                    foldAngle = 45f,
                    foldDuration = 5000,
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

