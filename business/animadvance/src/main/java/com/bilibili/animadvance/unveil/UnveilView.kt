package com.bilibili.animadvance.unveil

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import androidx.core.animation.addListener
import androidx.core.graphics.withSave
import com.bilibili.graphics.rect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.system.measureTimeMillis

class UnveilView
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
        private val cleanPainter =
            Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }

        private val state: MutableStateFlow<AnimState> = MutableStateFlow(AnimState())

        private var coverBm: Bitmap? = null
        private var brandBm: Bitmap? = null

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
            val edgeAnimator =
                ValueAnimator
                    .ofFloat(
                        params.containerHeight * params.edgeStart,
                        params.containerHeight * params.edgeEnd,
                    ).apply {
                        duration = params.edgeDuration
                        addUpdateListener {
                            state.value = state.value.copy(edgeY = it.animatedValue as Float)
                        }
                    }
            val peakAnimator =
                ValueAnimator
                    .ofFloat(
                        params.containerHeight * params.peakStart,
                        params.containerHeight * params.peakEnd,
                    ).apply {
                        duration = params.peakDuration
                        addUpdateListener {
                            state.value = state.value.copy(peakY = it.animatedValue as Float)
                        }
                    }

            val brandMoveAnimator =
                ValueAnimator
                    .ofFloat(
                        params.containerHeight * params.brandStart,
                        params.containerHeight * params.brandEnd,
                    ).apply {
                        duration = params.brandDuration
                        startDelay = params.brandStartDelay
                        addUpdateListener {
                            state.value = state.value.copy(brandCY = it.animatedValue as Float)
                        }
                    }

            AnimatorSet().apply {
                playTogether(edgeAnimator, peakAnimator, brandMoveAnimator)
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

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            state.value = state.value.copy(end = true)
        }

        private val frameCostMsList = mutableListOf<Long>()

        private val brandSrcRect by lazy {
            brandBm.rect
        }

        private val coverSrcRect by lazy {
            coverBm.rect
        }

        private fun drawAnim() {
            val cover = coverBm ?: return
            val brand = brandBm ?: return
            val canvas = lockCanvas() ?: return
            measureTimeMillis {
                canvas.drawPaint(cleanPainter)
                canvas.withSave {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        clipOutPath(state.value.alphaPath)
                    }

                    drawBitmap(
                        cover,
                        coverSrcRect,
                        Rect(
                            0,
                            0,
                            state.value.params.containerWidth
                                .toInt(),
                            state.value.params.containerHeight
                                .toInt(),
                        ),
                        null,
                    )
                    if (firstFrameRender != null) {
                        post {
                            firstFrameRender?.invoke()
                            firstFrameRender = null
                        }
                    }
                }
                canvas.drawBitmap(brand, brandSrcRect, state.value.brandRect, null)
            }.apply {
                frameCostMsList.add(this)
            }
            unlockCanvasAndPost(canvas)
        }

        data class AnimParams(
            val edgeStart: Float = 1f,
            val edgeEnd: Float = 0f,
            val edgeDuration: Long = 5000,
            val peakStart: Float = 0f,
            val peakEnd: Float = 0f,
            val peakDuration: Long = 3000,
            val containerWidth: Float = 0f,
            val containerHeight: Float = 0f,
            val brandWidth: Int = 0,
            val brandHeight: Int = 0,
            val brandStart: Float = 0f,
            val brandEnd: Float = 0f,
            val brandDuration: Long = 2000,
            val brandStartDelay: Long = 1000,
            val cx: Float = 0f,
        )

        private data class AnimState(
            val end: Boolean = false,
            val edgeY: Float = 0f,
            val peakY: Float = 0f,
            val brandCY: Float = 0f,
            val params: AnimParams = AnimParams(),
        ) {
            val alphaPath: Path
                get() =
                    Path().apply {
                        moveTo(params.cx, peakY)
                        cubicTo(params.cx, peakY, params.cx, edgeY, 0f, edgeY)
                        lineTo(0f, params.containerHeight)
                        lineTo(params.containerWidth, params.containerHeight)
                        lineTo(params.containerWidth, edgeY)
                        cubicTo(params.cx, edgeY, params.cx, peakY, params.cx, peakY)
                    }

            val brandRect: Rect
                get() =
                    Rect(
                        (params.cx - params.brandWidth / 2).toInt(),
                        (brandCY - params.brandHeight / 2).toInt(),
                        (params.cx + params.brandWidth / 2).toInt(),
                        (brandCY + params.brandHeight / 2).toInt(),
                    )
        }
    }
