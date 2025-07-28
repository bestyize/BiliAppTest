package com.bilibili.animadvance.foldimage

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import androidx.core.animation.addListener
import androidx.core.graphics.withSave
import com.bilibili.graphics.splitVertical
import com.bilibili.graphics.withSave
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

class FoldImage3DAnimationView
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

        // 3D 变换工具
        private val camera = Camera()
        private val leftMatrix = Matrix()
        private val rightMatrix = Matrix()
        private val tempMatrix = Matrix()

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
                ValueAnimator.ofFloat(0f, params.foldAngle).apply {
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

        private var leftCoverBm: Bitmap? = null

        private var rightCoverBm: Bitmap? = null

        private fun drawAnim() {
            val leftCover = leftCoverBm ?: return
            val rightCover = rightCoverBm ?: return
            brandBm ?: return
            val canvas = lockCanvas() ?: return
            measureTimeMillis {
                canvas.drawPaint(cleanPainter)

                do3DTransform(leftCover = leftCover, rightCover = rightCover)
                canvas.withSave {
                    // 计算绘制位置（居中）
                    val totalWidth = leftCover.width + rightCover.width
                    val startX = (width - totalWidth) / 2f
                    val startY = (height - max(leftCover.height, rightCover.height)) / 2f
                    Log.i("[read]", "startX = $startX, startY = $startY")
                    // 绘制左半部分
                    canvas.translate(startX, startY)
                    canvas.drawBitmap(leftCover, leftMatrix, null)
                    // 绘制右半部分
                    canvas.translate(leftCover.width.toFloat(), 0f)
                    canvas.drawBitmap(rightCover, rightMatrix, null)
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

        private fun do3DTransform(
            leftCover: Bitmap,
            rightCover: Bitmap,
        ) {
            val currAngle = state.value.currAngle
            camera.setLocation(0f, 0f, -state.value.params.foldDepth * resources.displayMetrics.density)
            camera.withSave {
                leftMatrix.reset()
                // 左半部分变换
                val centerX = leftCover.width.toFloat()
                val centerY = leftCover.height / 2f

                // 应用Camera变换
                camera.rotateY(-currAngle) // 绕Y轴旋转（负值表示向左旋转）
                camera.getMatrix(leftMatrix)

                // 调整矩阵使旋转中心在边缘
                leftMatrix.preTranslate(-centerX, -centerY)
                leftMatrix.postTranslate(centerX, centerY)

                // 添加透视效果
                tempMatrix.reset()
                tempMatrix.setScale(1f, 1f - min(0.2f, currAngle / 900))
                leftMatrix.postConcat(tempMatrix)
            }

            camera.withSave {
                rightMatrix.reset()

                // 右半部分变换
                val centerX = 0f // 绕左侧旋转
                val centerY = rightCover.height / 2f

                // 应用Camera变换
                camera.rotateY(currAngle) // 绕Y轴旋转（正值表示向右旋转）
                camera.getMatrix(rightMatrix)

                // 调整矩阵使旋转中心在边缘
                rightMatrix.preTranslate(-centerX, -centerY)
                rightMatrix.postTranslate(centerX, centerY)

                // 添加透视效果
                tempMatrix.reset()
                tempMatrix.setScale(1f, 1f - min(0.2f, currAngle / 900))
                rightMatrix.postConcat(tempMatrix)
            }
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            state.value = state.value.copy(end = true)
        }

        data class AnimParams(
            val foldAngle: Float = 0f,
            val foldDuration: Long = 0L,
            val foldDepth: Float = 20f,
            val foldPos: Float = 0.5f,
            val containerWidth: Float = 0f,
            val containerHeight: Float = 0f,
        )

        private data class AnimState(
            val end: Boolean = false,
            val params: AnimParams = AnimParams(),
            val currAngle: Float = 0f,
        )
    }
