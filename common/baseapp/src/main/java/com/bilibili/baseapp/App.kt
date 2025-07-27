package com.bilibili.baseapp

import android.app.Application
import android.widget.Toast

class App : Application() {
    override fun onCreate() {
        _app = this
        super.onCreate()
    }

    companion object {
        private var _app: App? = null

        val app: App by lazy { _app!! }

    }
}

fun toast(msg: String?) {
    msg ?: return
    Toast.makeText(App.app, msg, Toast.LENGTH_SHORT).show()
}