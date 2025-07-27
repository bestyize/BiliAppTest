package com.bilibili.animadvance.foldimage

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import androidx.core.animation.addListener
import kotlinx.coroutines.flow.MutableStateFlow

class FoldImageAnimationView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defAttr: Int = 0,
) : TextureView(
    context, attributeSet, defAttr
) {


    private val state: MutableStateFlow<AnimState> = MutableStateFlow(AnimState())

    private var coverBm: Bitmap? = null
    private var brandBm: Bitmap? = null

    private val cleanPainter = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val frameCostMsList = mutableListOf<Long>()

    private var firstFrameRender: (() -> Unit)? = null

    init {
        isOpaque = false
    }

    fun startAnimation(
        coverBm: Bitmap,
        brandBm: Bitmap,
        params: AnimParams,
        onFirstFrameRender: () -> Unit,
        onEnd: () -> Unit,
    ) {
        this.coverBm = coverBm
        this.brandBm = brandBm
        state.value = state.value.copy(params = params)
        val foldAnim = ValueAnimator.ofFloat(0f, 45f).apply {
            duration = params.foldDuration
            addUpdateListener {
                val angle = it.animatedValue as Float
                state.value = state.value.copy(currAngle = angle)
            }
        }
        AnimatorSet().apply {
            playTogether(foldAnim)
            addListener(onEnd = {
                state.value = state.value.copy(end = true)
                onEnd.invoke()
                Log.i("[read]", "avgCost = ${frameCostMsList.average()}")
            })
            start()
        }

        firstFrameRender = onFirstFrameRender
        Thread {
            while (!state.value.end) {
                drawAnim()
            }
        }.start()
    }

    private fun drawAnim() {
        coverBm ?: return
        brandBm ?: return
        val canvas = lockCanvas() ?: return
        canvas.drawPaint(cleanPainter)

        unlockCanvasAndPost(canvas)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        state.value = state.value.copy(end = true)
    }


    data class AnimParams(
        val foldAngle: Float = 0f,
        val foldDuration: Long = 0L,
        val containerWidth: Float = 0f,
        val containerHeight: Float = 0f,
    )


    private data class AnimState(
        val end: Boolean = false,
        val params: AnimParams = AnimParams(),
        val currAngle: Float = 0f,
    )


}