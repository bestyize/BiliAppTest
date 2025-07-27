package com.bilibili.biliapptest

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.doOnPreDraw
import androidx.core.view.drawToBitmap
import com.bilibili.animadvance.unveil.UnveilView
import com.bilibili.biliapptest.databinding.ActivityMainBinding
import com.bilibili.biliapptest.index.IndexPage

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContent {
            IndexPage()
        }

    }

}