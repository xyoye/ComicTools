package com.xyoye.comictools

import android.app.Application
import com.blankj.utilcode.util.Utils

/**
 * Created by xyoye on 2020/1/14.
 */

class IApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
    }
}