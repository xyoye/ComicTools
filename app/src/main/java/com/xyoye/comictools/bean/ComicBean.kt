package com.xyoye.comictools.bean

/**
 * Created by xyoye on 2020/1/16.
 */

class ComicBean {
    var comicId: String? = null
    var comicName: String? = null
    var chapterList: List<ChapterBean>? = null

    class ChapterBean {
        var chapterId: String? = null
        var chapterName: String? = null
        var chapterNum: String? = null
        var chapterIndexPath: String? = null
        var episodeList: List<EpisodeBean>? = null

        class EpisodeBean {
            var episodePath: String? = null
            var episodeIndex: Int = 0
        }
    }
}
