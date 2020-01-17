package com.xyoye.comictools.utils

import com.google.gson.Gson

/**
 * Created by xyoye on 2020/1/15.
 */

object JsonUtils {
    fun <T> fromJson(jsonStr: String?, clazz: Class<T>): T? {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return null
        }
        try {
            return Gson().fromJson(jsonStr, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun toJson(`object`: Any): String? {
        var json: String? = null
        try {
            json = Gson().toJson(`object`)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return json
    }
}