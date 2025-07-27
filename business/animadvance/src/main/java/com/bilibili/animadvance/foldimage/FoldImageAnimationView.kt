package com.bilibili.animadvance.foldimage

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import androidx.core.animation.addListener
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.system.measureTimeMillis

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
        coverBm.splitVertical().run {
            leftCoverBm = first
            rightCoverBm = second
        }
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

    private var leftCoverBm: Bitmap? = null

    private var rightCoverBm: Bitmap? = null

    private fun drawAnim() {
        val leftCover = leftCoverBm ?: return
        val rightCover = rightCoverBm ?: return
        brandBm ?: return
        val canvas = lockCanvas() ?: return
        canvas.drawPaint(cleanPainter)

        measureTimeMillis {
            canvas.withSave {
                val angle = state.value.currAngle
                val left = rotateBitmapAroundAxis(leftCover, true, angle)
                val right = rotateBitmapAroundAxis(rightCover, false, angle)
                val combine = combineRotatedParts(left = left, right = right)
                canvas.drawBitmap(combine, combine.originRect, combine.originRect, null)
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

    /**
     * 扩展函数：垂直分割图片为左右两半
     */
    private fun Bitmap.splitVertical(): Pair<Bitmap, Bitmap> {
        val leftWidth = width / 2
        val rightWidth = width - leftWidth

        return Pair(
            Bitmap.createBitmap(this, 0, 0, leftWidth, height),
            Bitmap.createBitmap(this, leftWidth, 0, rightWidth, height)
        )
    }

    /**
     * 绕垂直轴旋转图片
     * @param bitmap 要旋转的图片
     * @param isLeft 是否是左半部分
     * @param angle 旋转角度（度）
     */
    private fun rotateBitmapAroundAxis(bitmap: Bitmap, isLeft: Boolean, angle: Float): Bitmap {
        // 计算旋转后需要的新画布尺寸
        val radians = Math.toRadians(angle.toDouble())
        val newWidth = (bitmap.width * cos(radians) + bitmap.height * sin(radians)).toInt()
        val newHeight = (bitmap.height * cos(radians) + bitmap.width * sin(radians)).toInt()

        // 创建新位图
        val rotatedBitmap = createBitmap(newWidth, newHeight)
        val canvas = Canvas(rotatedBitmap)

        // 设置旋转中心（对于左图是右侧中点，对于右图是左侧中点）
        val pivotX = if (isLeft) bitmap.width.toFloat() else 0f
        val pivotY = bitmap.height / 2f

        // 计算平移到画布中央的偏移量
        val offsetX = (newWidth - bitmap.width) / 2f
        val offsetY = (newHeight - bitmap.height) / 2f

        // 应用矩阵变换
        val matrix = Matrix().apply {
            if (isLeft) {
                // 左半部分绕右侧中点逆时针旋转
                postRotate(-angle, pivotX, pivotY)
            } else {
                // 右半部分绕左侧中点顺时针旋转
                postRotate(angle, pivotX, pivotY)
            }
            // 平移使旋转中心对齐新画布中心
            postTranslate(offsetX, offsetY)
        }

        // 绘制旋转后的位图
        canvas.drawBitmap(bitmap, matrix, null)

        return rotatedBitmap
    }

    /**
     * 组合两个旋转后的部分
     */
    private fun combineRotatedParts(left: Bitmap, right: Bitmap): Bitmap {
        // 计算新画布尺寸
        val newWidth = left.width + right.width
        val newHeight = max(left.height, right.height)

        // 创建组合位图
        val combined = createBitmap(newWidth, newHeight)
        val canvas = Canvas(combined)

        // 放置左半部分（调整位置使其右侧中点对齐中心轴）
        val leftX = (newWidth / 2) - left.width
        val leftY = (newHeight - left.height) / 2f
        canvas.drawBitmap(left, leftX.toFloat(), leftY, null)

        // 放置右半部分（调整位置使其左侧中点对齐中心轴）
        val rightX = (newWidth / 2f)
        val rightY = (newHeight - right.height) / 2f
        canvas.drawBitmap(right, rightX, rightY, null)

        return combined
    }

    private val Bitmap.originRect: Rect get() = Rect(0, 0, width, height)
}
