package com.bilibili.biliapptest.index

import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bilibili.animadvance.router.AdvanceAnimPage
import com.bilibili.bilirouter.BiliRouterPage
import com.example.viewwidget.stand.OptionView

@Composable
fun IndexPage() {
    Box(
        modifier =
            Modifier
                .statusBarsPadding()
                .fillMaxSize(),
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "/advanceAnimation",
            enterTransition = {
                slideIn { intSize ->
                    IntOffset(0, 0)
                }
            },
            exitTransition = {
                slideOut { intSize ->
                    IntOffset(0, 0)
                }
            },
        ) {
            composable("/advanceAnimation") {
                AdvanceAnimPage()
            }
            composable("/biliRouter") {
                BiliRouterPage()
            }
        }

        FlowRow(
            modifier =

                Modifier
                    .wrapContentSize()
                    .align(Alignment.BottomEnd)
                    .padding(25.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OptionView("各种动画") {
                navController.navigate("/advanceAnimation")
            }

            OptionView("各种路由") {
                navController.navigate("/biliRouter")
            }
        }
    }
}
