package com.bilibili.animadvance.foldimage

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import kotlin.math.max
import kotlin.math.min

class FoldableView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 图片资源
    private var originalBitmap: Bitmap? = null
    private var leftBitmap: Bitmap? = null
    private var rightBitmap: Bitmap? = null

    // 3D 变换工具
    private val camera = Camera()
    private val leftMatrix = Matrix()
    private val rightMatrix = Matrix()
    private val tempMatrix = Matrix()

    // 折叠角度
    private var foldAngle = 0f

    // 中心轴位置
    private var foldPosition = 0.5f

    // 折叠深度（透视效果）
    private var foldDepth = 20f

    // 阴影效果
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, 0f, 0f,
            0x55000000, 0x00000000, Shader.TileMode.CLAMP
        )
        maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
    }

    // 背景画刷
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.transparent)
    }

    // 当前动画进度
    private var animProgress = 0f

    // 是否正在动画
    private var isAnimating = false

    init {
        // 初始化时绘制一些视觉效果
        setLayerType(LAYER_TYPE_HARDWARE, null) // 启用硬件加速
    }

    fun setBitmap(bitmap: Bitmap) {
        // 清除旧位图
        originalBitmap?.recycle()
        leftBitmap?.recycle()
        rightBitmap?.recycle()

        // 设置新位图
        originalBitmap = bitmap
        prepareBitmaps()
        invalidate()
    }

    fun foldToAngle(angle: Float, duration: Long = 1000) {
        val animator = ValueAnimator.ofFloat(foldAngle, angle).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener {
                foldAngle = it.animatedValue as Float
                updateMatrices()
                invalidate()
            }
        }
        animator.start()
    }

    fun foldAndUnfold(duration: Long = 2000) {
        val animator = ValueAnimator.ofFloat(0f, 1f, 0f).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                animProgress = it.animatedValue as Float
                isAnimating = true
                foldAngle = if (animProgress < 0.5f) {
                    animProgress * 180f // 从0到180度折叠
                } else {
                    (1f - animProgress) * 180f // 从180度展开
                }
                updateMatrices()
                invalidate()
            }
            doOnEnd {
                isAnimating = false
            }
        }
        animator.start()
    }

    private fun prepareBitmaps() {
        originalBitmap?.let { bmp ->
            // 分割位图
            val width = bmp.width
            val height = bmp.height

            // 计算分割位置
            val foldPosX = (width * foldPosition).toInt()

            // 左半部分
            leftBitmap = Bitmap.createBitmap(bmp, 0, 0, foldPosX, height)

            // 右半部分
            rightBitmap = Bitmap.createBitmap(bmp, foldPosX, 0, width - foldPosX, height)
        }
        updateMatrices()
    }

    private fun updateMatrices() {
        leftBitmap?.let { left ->
            camera.save()
            leftMatrix.reset()

            // 左半部分变换
            val centerX = left.width.toFloat()
            val centerY = left.height / 2f

            // 应用Camera变换
            camera.rotateY(-foldAngle) // 绕Y轴旋转（负值表示向左旋转）
            camera.getMatrix(leftMatrix)

            // 调整矩阵使旋转中心在边缘
            leftMatrix.preTranslate(-centerX, -centerY)
            leftMatrix.postTranslate(centerX, centerY)

            // 添加透视效果
            tempMatrix.reset()
            tempMatrix.setScale(1f, 1f - min(0.2f, foldAngle / 900))
            leftMatrix.postConcat(tempMatrix)

            camera.restore()
        }

        rightBitmap?.let { right ->
            camera.save()
            rightMatrix.reset()

            // 右半部分变换
            val centerX = 0f // 绕左侧旋转
            val centerY = right.height / 2f

            // 应用Camera变换
            camera.rotateY(foldAngle) // 绕Y轴旋转（正值表示向右旋转）
            camera.getMatrix(rightMatrix)

            // 调整矩阵使旋转中心在边缘
            rightMatrix.preTranslate(-centerX, -centerY)
            rightMatrix.postTranslate(centerX, centerY)

            // 添加透视效果
            tempMatrix.reset()
            tempMatrix.setScale(1f, 1f - min(0.2f, foldAngle / 900))
            rightMatrix.postConcat(tempMatrix)

            camera.restore()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 视图大小变化时重新准备位图
        prepareBitmaps()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制背景
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        leftBitmap?.let { left ->
            rightBitmap?.let { right ->
                // 计算绘制位置（居中）
                val totalWidth = left.width + right.width
                val startX = (width - totalWidth) / 2f
                val startY = (height - max(left.height, right.height)) / 2f

                // 保存画布状态
                canvas.save()

                // 绘制左半部分
                canvas.translate(startX, startY)
                canvas.drawBitmap(left, leftMatrix, null)

                // 添加左半部分的阴影效果
                if (foldAngle > 0) {
                    shadowPaint.alpha = (min(220f, foldAngle * 1.5f)).toInt()
                    canvas.drawRect(
                        0f, 0f, left.width.toFloat(), left.height.toFloat(),
                        shadowPaint
                    )
                }

                // 绘制右半部分
                canvas.translate(left.width.toFloat(), 0f)
                canvas.drawBitmap(right, rightMatrix, null)

                // 添加右半部分的阴影效果
                if (foldAngle > 0) {
                    shadowPaint.alpha = (min(220f, foldAngle * 1.5f)).toInt()
                    canvas.drawRect(
                        0f, 0f, right.width.toFloat(), right.height.toFloat(),
                        shadowPaint
                    )
                }

                // 恢复画布状态
                canvas.restore()
            }
        }
    }

    fun setFoldPosition(position: Float) {
        foldPosition = position.coerceIn(0.1f, 0.9f)
        prepareBitmaps()
        invalidate()
    }

    fun setFoldDepth(depth: Float) {
        foldDepth = depth
        updateMatrices()
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 清除位图资源
        originalBitmap?.recycle()
        leftBitmap?.recycle()
        rightBitmap?.recycle()
        originalBitmap = null
        leftBitmap = null
        rightBitmap = null
    }
}