package com.xyoye.comictools.utils

import com.xyoye.comictools.bean.ComicNetworkInfo

/**
 * Created by xyoye on 2020/1/16.
 */

interface NetworkCallback {
    fun onSuccess(comicInfo: ComicNetworkInfo)

    fun onFailed()
}
