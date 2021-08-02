package com.alexparra.chatapp

import android.app.Application
import android.content.Context

class MainApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private lateinit var instance: Application

        fun applicationContext(): Context {
            return instance.applicationContext
        }
    }
}