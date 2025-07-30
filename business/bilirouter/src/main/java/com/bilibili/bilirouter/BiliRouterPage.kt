package com.bilibili.bilirouter

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.viewwidget.stand.OptionView

@Composable
fun BiliRouterPage() {
    val activity = LocalActivity.current
    FlowRow(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OptionView("天马TAB") {
            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    "bilibili://home?tab_id=推荐tab&bottom_tab_name=%E9%A6%96%E9%A1%B5".toUri(),
                )
            activity?.startActivity(intent)
        }

        OptionView("直播TAB") {
            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    "bilibili://home?tab_id=直播tab&bottom_tab_name=%E9%A6%96%E9%A1%B5".toUri(),
                )
            activity?.startActivity(intent)
        }

        OptionView("详情页") {
            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    "bilibili://video/114849069663662?cid=31024678824".toUri(),
                )
            activity?.startActivity(intent)
        }
    }
}
