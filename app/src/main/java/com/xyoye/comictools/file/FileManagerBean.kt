package com.xyoye.comictools.file

import java.io.File
import java.io.Serializable

/**
 * Created by xyoye on 2018/7/2.
 */

class FileManagerBean(var file: File, var name: String, var isParent: Boolean) : Serializable
