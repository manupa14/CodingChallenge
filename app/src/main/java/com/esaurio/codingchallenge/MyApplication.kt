package com.esaurio.codingchallenge

import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        myInstance = this
    }

    companion object {
        private var myInstance : MyApplication? = null
        val instance : MyApplication
            get() = myInstance!!
    }
}