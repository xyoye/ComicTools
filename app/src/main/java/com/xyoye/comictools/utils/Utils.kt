package com.xyoye.comictools.utils

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
}