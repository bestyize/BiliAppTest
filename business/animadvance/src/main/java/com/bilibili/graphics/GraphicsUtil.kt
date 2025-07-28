package com.bilibili.graphics

import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import androidx.core.graphics.withMatrix

val Bitmap?.rect: Rect
    get() = if (this == null) Rect() else Rect(0, 0, width, height)

fun Bitmap.splitVertical(): Pair<Bitmap, Bitmap> {
    val leftWidth = width / 2
    val rightWidth = width - leftWidth

    return Pair(
        Bitmap.createBitmap(this, 0, 0, leftWidth, height),
        Bitmap.createBitmap(this, leftWidth, 0, rightWidth, height),
    )
}

fun drawBitmapWithPerspective(
    canvas: Canvas,
    bitmap: Bitmap,
    topLeft: PointF,
    topRight: PointF,
    bottomRight: PointF,
    bottomLeft: PointF,
) {
    // 创建源点坐标数组（原始图像的四个角）
    val src =
        floatArrayOf(
            0f,
            0f, // 左上
            bitmap.width.toFloat(),
            0f, // 右上
            bitmap.width.toFloat(),
            bitmap.height.toFloat(), // 右下
            0f,
            bitmap.height.toFloat(), // 左下
        )

    // 创建目标点坐标数组（梯形四个角）
    val dst =
        floatArrayOf(
            topLeft.x,
            topLeft.y, // 左上
            topRight.x,
            topRight.y, // 右上
            bottomRight.x,
            bottomRight.y, // 右下
            bottomLeft.x,
            bottomLeft.y, // 左下
        )

    // 计算变换矩阵
    val matrix = Matrix()
    val result =
        matrix.setPolyToPoly(
            src, // 源点坐标数组
            0, // 源点数组偏移量
            dst, // 目标点坐标数组
            0, // 目标点数组偏移量
            4, // 点数量（4表示四边形）
        )

    if (result) {
        canvas.withMatrix(matrix) {
            drawBitmap(bitmap, 0f, 0f, null)
        }
    }
}


inline fun Camera.withSave(block: Camera.() -> Unit) {
    save()
    try {
        block()
    } finally {
        restore()
    }
}
