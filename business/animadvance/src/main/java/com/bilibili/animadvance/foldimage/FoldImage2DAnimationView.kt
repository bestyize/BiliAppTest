package com.bilibili.animadvance.foldimage

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import androidx.core.animation.addListener
import com.bilibili.graphics.drawBitmapWithPerspective
import com.bilibili.graphics.splitVertical
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.system.measureTimeMillis

class FoldImage2DAnimationView
    @JvmOverloads
    constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defAttr: Int = 0,
    ) : TextureView(
            context,
            attributeSet,
            defAttr,
        ) {
        private val state: MutableStateFlow<AnimState> = MutableStateFlow(AnimState())

        private var coverBm: Bitmap? = null
        private var brandBm: Bitmap? = null

        private val cleanPainter =
            Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }

        private val frameCostMsList = mutableListOf<Long>()

        private var firstFrameRender: (() -> Unit)? = null

        init {
            isOpaque = false
            setLayerType(LAYER_TYPE_HARDWARE, null) // 启用硬件加速
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
            coverBm.splitVertical().run {
                leftCoverBm = first
                rightCoverBm = second
            }
            state.value = state.value.copy(params = params)
            val foldAnim =
                ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = params.foldDuration
                    addUpdateListener {
                        val progress = it.animatedValue as Float
                        state.value = state.value.copy(progress = progress)
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

        private var leftCoverBm: Bitmap? = null

        private var rightCoverBm: Bitmap? = null

        private fun drawAnim() {
            val leftCover = leftCoverBm ?: return
            val rightCover = rightCoverBm ?: return
            brandBm ?: return
            val canvas = lockCanvas() ?: return
            measureTimeMillis {
                canvas.drawPaint(cleanPainter)

                state.value.run {
                    drawBitmapWithPerspective(
                        canvas = canvas,
                        bitmap = leftCover,
                        topLeft = leftTop,
                        topRight = centerTop,
                        bottomLeft = leftBottom,
                        bottomRight = centerBottom,
                    )
                    drawBitmapWithPerspective(
                        canvas = canvas,
                        bitmap = rightCover,
                        topLeft = centerTop,
                        topRight = rightTop,
                        bottomLeft = centerBottom,
                        bottomRight = rightBottom,
                    )
                }

                if (firstFrameRender != null) {
                    post {
                        firstFrameRender?.invoke()
                        firstFrameRender = null
                    }
                }
            }.let {
                frameCostMsList.add(it)
            }

            unlockCanvasAndPost(canvas)
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            state.value = state.value.copy(end = true)
        }

        data class AnimParams(
            val foldDuration: Long = 0L,
            val verticalOffset: Int = 0,
            val containerWidth: Float = 0f,
            val containerHeight: Float = 0f,
        )

        private data class AnimState(
            val end: Boolean = false,
            val params: AnimParams = AnimParams(),
            val progress: Float = 0f,
        ) {
            val leftTop: PointF
                get() =
                    PointF(
                        params.containerWidth / 2 * progress,
                        params.verticalOffset * progress,
                    )

            val centerTop: PointF by lazy {
                PointF(
                    params.containerWidth / 2,
                    0f,
                )
            }

            val centerBottom: PointF by lazy {
                PointF(
                    params.containerWidth / 2,
                    params.containerHeight,
                )
            }

            val leftBottom: PointF
                get() =
                    PointF(
                        params.containerWidth / 2 * progress,
                        params.containerHeight - params.verticalOffset * progress,
                    )

            val rightTop: PointF
                get() =
                    PointF(
                        params.containerWidth - params.containerWidth / 2 * progress,
                        params.verticalOffset * progress,
                    )
            val rightBottom: PointF
                get() =
                    PointF(
                        params.containerWidth - params.containerWidth / 2 * progress,
                        params.containerHeight - params.verticalOffset * progress,
                    )
        }
    }
