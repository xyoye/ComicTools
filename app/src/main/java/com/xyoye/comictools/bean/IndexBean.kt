package com.xyoye.comictools.bean

import com.blankj.utilcode.util.FileUtils

/**
 * Created by xyoye on 2020/1/15.
 */

class IndexBean {
    var pics: List<String>? = null
    var indexList: ArrayList<String> = ArrayList()

    public fun indexOf(episodeId: String): Int {
        if (pics == null || pics!!.isEmpty())
            return 0
        if (indexList.size < 1){
            indexList = ArrayList()
            for (pic in pics!!) {
                indexList.add(FileUtils.getFileNameNoExtension(pic))
            }
        }
        return indexList.indexOf(episodeId) + 1
    }
}
