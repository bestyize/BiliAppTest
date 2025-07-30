package com.bilibili.animadvance.router

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bilibili.animadvance.test.FoldImage2DTestActivity
import com.bilibili.animadvance.test.FoldImage3DTestActivity
import com.bilibili.animadvance.test.ShapeCutTestActivity
import com.bilibili.animadvance.test.UnveilTestActivity
import com.example.viewwidget.stand.OptionView

@Composable
fun AdvanceAnimPage() {
    val activity = LocalActivity.current
    FlowRow(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OptionView(title = "揭幕动画") {
            activity?.startActivity(
                Intent(
                    activity,
                    UnveilTestActivity::class.java,
                ),
            )
        }
        OptionView(title = "剪影动画") {
            activity?.startActivity(
                Intent(
                    activity,
                    ShapeCutTestActivity::class.java,
                ),
            )
        }

        OptionView(title = "折叠动画3D") {
            activity?.startActivity(
                Intent(
                    activity,
                    FoldImage3DTestActivity::class.java,
                ),
            )
        }

        OptionView(title = "折叠动画2D") {
            activity?.startActivity(
                Intent(
                    activity,
                    FoldImage2DTestActivity::class.java,
                ),
            )
        }
    }
}
