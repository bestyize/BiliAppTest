package com.bilibili.animadvance.shapecut

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import androidx.core.animation.addListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.system.measureTimeMillis

class ShapeCutAnimationView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defAttr: Int = 0,
) : TextureView(
    context, attributeSet, defAttr
) {

    private val state: MutableStateFlow<AnimState> = MutableStateFlow(AnimState())

    private val cleanPainter = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val shapePaint = Paint()

    private val shapeAppearPaint = Paint().apply {
        alpha = 0
    }


    private var coverBm: Bitmap? = null
    private var brandBm: Bitmap? = null

    private var firstFrameRender: (() -> Unit)? = null
    private val frameCostMsList = mutableListOf<Long>()

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
        state.value = state.value.copy(
            params = params
        )
        val scaleAnim =
            ValueAnimator.ofFloat(params.scaleInitPercent, params.scaleEndPercent).apply {
                duration = params.scaleDuration
                addUpdateListener {
                    state.value = state.value.copy(
                        shapePercent = it.animatedValue as Float
                    )
                }
            }

        val alphaAnim = ValueAnimator.ofInt(255, 0).apply {
            duration = params.alphaDuration
            addUpdateListener {
                state.value = state.value.copy(
                    alpha = it.animatedValue as Int
                )
            }
        }

        AnimatorSet().apply {
            playTogether(scaleAnim, alphaAnim)
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

    private val brandSrcRect by lazy {
        Rect(
            0, 0, brandBm!!.width, brandBm!!.height
        )
    }

    private val coverSrcRect by lazy {
        Rect(
            0, 0, coverBm!!.width, coverBm!!.height
        )
    }

    private val coverDstRect by lazy {
        Rect(
            0,
            0,
            state.value.params.containerWidth.toInt(),
            state.value.params.containerHeight.toInt()
        )
    }

    private fun drawAnim() {
        val cover = coverBm ?: return
        val brand = brandBm ?: return
        val canvas = lockCanvas() ?: return
        canvas.drawPaint(cleanPainter)
        measureTimeMillis {
            val shapeCurrRect = state.value.currentShapeRect
            // 1. 先绘制遮罩图（作为DST）
            canvas.drawBitmap(brand, brandSrcRect, shapeCurrRect, null)
            // 2. 设置混合模式：仅保留目标图与遮罩非透明区域的重叠部分
            shapePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            shapePaint.alpha = state.value.alpha
            // 3. 绘制目标图,带透明通道（作为SRC）
            canvas.drawBitmap(cover, coverSrcRect, coverDstRect, shapePaint)
            // 4. 清除Xfermode避免影响后续绘制
            shapePaint.xfermode = null
            // 5. 将结果绘制到View的Canvas
            shapeAppearPaint.alpha = 255 - state.value.alpha
            canvas.drawBitmap(brand, brandSrcRect, shapeCurrRect, shapeAppearPaint)
        }.let {
            frameCostMsList.add(it)
        }
        if (firstFrameRender != null) {
            post {
                firstFrameRender?.invoke()
                firstFrameRender = null
            }
        }
        unlockCanvasAndPost(canvas)

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        state.value = state.value.copy(end = true)
    }


    data class AnimParams(
        val targetRect: Rect = Rect(),
        val scaleInitPercent: Float = 50f,
        val scaleEndPercent: Float = 1f,
        val scaleDuration: Long = 0L,
        val alphaDuration: Long = 0L,
        val containerWidth: Float = 0f,
        val containerHeight: Float = 0f,
    )

    private data class AnimState(
        val end: Boolean = false,
        val params: AnimParams = AnimParams(),
        val shapePercent: Float = 0f,
        val alpha: Int = 255,
    ) {
        val currentShapeRect: Rect
            get() = params.targetRect.scaleTo(shapePercent)

    }


}

private fun Rect.scaleTo(scale: Float): Rect {
    val newWidth = (width() * scale).toInt()
    val newHeight = (height() * scale).toInt()

    val cx = centerX()
    val cy = centerY()

    return Rect(
        cx - newWidth / 2,
        cy - newHeight / 2,
        cx + newWidth / 2,
        cy + newHeight / 2,
    )
}
