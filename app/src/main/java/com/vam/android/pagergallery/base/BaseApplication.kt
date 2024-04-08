package com.vam.android.pagergallery.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class BaseApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val TOKEN = "//填入自己的TOKEN"
        var SAFE_SEARCH = true

    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}
