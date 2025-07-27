package com.bilibili.animadvance.foldimage

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView

class FoldImageAnimationView  @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defAttr: Int = 0,
) : TextureView(
    context, attributeSet, defAttr
) {
    init {
        isOpaque = false
    }


}