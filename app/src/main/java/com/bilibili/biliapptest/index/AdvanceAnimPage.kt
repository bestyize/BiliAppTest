package com.bilibili.biliapptest.index

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bilibili.animadvance.test.FoldImageTestActivity
import com.bilibili.animadvance.test.ShapeCutTestActivity
import com.bilibili.animadvance.test.UnveilTestActivity

@Composable
fun AdvanceAnimPage() {
    val activity = LocalActivity.current
    FlowRow(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OptionView(title = "揭幕动画") {
            activity?.startActivity(
                Intent(
                    activity, UnveilTestActivity::class.java
                )
            )
        }
        OptionView(title = "剪影动画") {
            activity?.startActivity(
                Intent(
                    activity, ShapeCutTestActivity::class.java
                )
            )
        }

        OptionView(title = "折叠动画") {
            activity?.startActivity(
                Intent(
                    activity, FoldImageTestActivity::class.java
                )
            )
        }
    }
}

@Preview
@Composable
private fun OptionView(title: String = "", onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .background(color = Color(0xFFFF6699), shape = RoundedCornerShape(8.dp))
            .clickable {
                onClick.invoke()
            }
            .padding(10.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = title, color = Color.White, fontSize = 16.sp
        )
    }
}