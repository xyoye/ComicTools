package com.xyoye.comictools.bean

/**
 * Created by xyoye on 2020/1/16.
 */

class ComicNetworkInfo {
    /**
     * code : 0
     * data : {"id":25756,"title":"五等分的新娘","ep_list":[{"id":354234,"short_title":"福利","title":"《五等分的新娘》应援福利"}]}
     */

    var code: Int = 0
    var data: DataBean? = null

    class DataBean {
        /**
         * id : 25756
         * title : 五等分的新娘
         * ep_list : [{"id":354234,"short_title":"福利","title":"《五等分的新娘》应援福利"}]
         */

        var id: Int = 0
        var title: String? = null
        var ep_list: ArrayList<EpListBean>? = null

        class EpListBean {
            /**
             * id : 354234
             * short_title : 福利
             * title : 《五等分的新娘》应援福利
             */

            var id: Int = 0
            var short_title: String? = null
            var title: String? = null
        }
    }
}
