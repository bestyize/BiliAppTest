package com.example.viewwidget.stand

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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

@Preview
@Composable
fun OptionView(
    title: String = "",
    onClick: () -> Unit = {},
) {
    Box(
        modifier =
            Modifier
                .wrapContentSize()
                .background(color = Color(0xFFFF6699), shape = RoundedCornerShape(8.dp))
                .clickable {
                    onClick.invoke()
                }.padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
        )
    }
}
