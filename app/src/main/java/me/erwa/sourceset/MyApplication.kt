package me.erwa.sourceset

import android.app.Application

/**
 * @author: drawf
 * @date: 2019/4/16
 * @see: <a href=""></a>
 * @description:
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MyApplication.context = applicationContext as Application
    }

    companion object {
        lateinit var context: Application
    }
}