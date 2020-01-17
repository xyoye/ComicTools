package com.xyoye.comictools.utils

import com.google.gson.Gson
import java.util.regex.Pattern

/**
 * Created by xyoye on 2020/1/15.
 */

object Utils {
    //字符串是否为数字
    fun isNum(str: String): Boolean {
        val pattern = Pattern.compile("^[\\d]*$")
        return pattern.matcher(str).matches()
    }

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
}