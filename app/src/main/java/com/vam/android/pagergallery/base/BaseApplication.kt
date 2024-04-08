package com.vam.android.pagergallery.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class BaseApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val TOKEN = "40746625-6dd3135dd043554f3b934d632"
        var SAFE_SEARCH = true

    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}