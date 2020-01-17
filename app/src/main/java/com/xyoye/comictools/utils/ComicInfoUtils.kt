package com.xyoye.comictools.utils

import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ToastUtils
import com.xyoye.comictools.bean.ComicBean
import com.xyoye.comictools.bean.ComicNetworkInfo
import com.xyoye.comictools.file.FileManagerDialog
import java.io.File

/**
 * Created by xyoye on 2020/1/17.
 */

object ComicInfoUtils {

    /**
     * get comic info by BiliBili cache folder or other cache folder
     */
    fun getComicData(folderPath: String): ComicBean? {
        val folderFile = File(folderPath)
        if (!folderFile.exists() || !folderFile.isDirectory) {
            ToastUtils.showLong("文件夹路径错误")
            return null
        }

        if (folderFile.listFiles().isEmpty()) {
            ToastUtils.showLong("文件夹为空")
            return null
        }

        return if (checkComicFolder(folderPath))
            getBiliBiliComicInfo(folderPath)
        else
            getOtherComicInfo(folderPath)
    }

    /**
     * is BiliBili cache folder
     *
     * the right rule : BiliBili_Cache_Folder/123(the comic id, must be a number)
     */
    private fun checkComicFolder(folderPath: String): Boolean {
        if (!folderPath.startsWith(FileManagerDialog.COMIC_CACHE_PATH))
            return false
        var comicId = folderPath.substring(FileManagerDialog.COMIC_CACHE_PATH.length)
        if (comicId.startsWith("/"))
            comicId = comicId.substring(1, comicId.length)
        return Utils.isNum(comicId)
    }

    private fun getBiliBiliComicInfo(folderPath: String): ComicBean {
        //ergodic comic folder
        val comicFile = File(folderPath)
        val comicBean = ComicBean()
        comicBean.comicId = comicFile.name
        val chapterList = ArrayList<ComicBean.ChapterBean>()
        for (chapterFile in comicFile.listFiles()) {
            if (chapterFile.isDirectory && Utils.isNum(chapterFile.name)) {
                val chapterBean = ComicBean.ChapterBean()
                chapterBean.chapterId = chapterFile.name
                val episodeList = ArrayList<ComicBean.ChapterBean.EpisodeBean>()
                for (episodeFile in chapterFile.listFiles()) {
                    if (episodeFile.absolutePath.endsWith(".jpg.view")) {
                        val episodeBean = ComicBean.ChapterBean.EpisodeBean()
                        episodeBean.episodePath = episodeFile.absolutePath
                        episodeList.add(episodeBean)
                    } else if (episodeFile.name == "index.dat") {
                        chapterBean.chapterIndexPath = episodeFile.absolutePath
                    }
                }
                chapterBean.episodeList = episodeList
                chapterList.add(chapterBean)
            }
        }
        comicBean.chapterList = chapterList

        //get more info by network
        fillInfoByNetWork(comicBean)

        //get more info by index.dat file
        fillInfoByIndexFile(comicBean)

        return comicBean
    }

    private fun fillInfoByNetWork(comicBean: ComicBean) {
        //get comic info by comic id
        NetworkUtils.getComicInfo(comicBean.comicId!!, object : NetworkCallback {
            override fun onSuccess(comicInfo: ComicNetworkInfo) {
                comicBean.comicName = comicInfo.data?.title
                for (chapter in comicBean.chapterList!!) {
                    val infoIterator = comicInfo.data?.ep_list!!.iterator()
                    BreakTag@ while (infoIterator.hasNext()) {
                        val epBean = infoIterator.next()
                        if (epBean.id.toString() == chapter.chapterId) {
                            chapter.chapterName = epBean.title
                            chapter.chapterNum = epBean.short_title
                            infoIterator.remove()
                            break@BreakTag
                        }
                    }
                }
            }

            override fun onFailed() {
                comicBean.comicName = comicBean.comicId
                for (chapter in comicBean.chapterList!!) {
                    chapter.chapterNum = chapter.chapterId
                }
            }
        })
    }

    private fun fillInfoByIndexFile(comicBean: ComicBean) {
        for (chapter in comicBean.chapterList!!) {
            if (!chapter.chapterIndexPath.isNullOrBlank()) {
                val indexBean = IndexUtils.getIndexInfo(
                    chapter.chapterIndexPath!!,
                    comicBean.comicId!!,
                    chapter.chapterId!!
                )
                for (episode in chapter.episodeList!!) {
                    val jpgPath = FileUtils.getFileNameNoExtension(episode.episodePath)
                    episode.episodeIndex =
                        indexBean!!.indexOf(FileUtils.getFileNameNoExtension(jpgPath))
                }
            }
        }
    }

    /**
     * not BiliBili cache folder
     *
     * the right rule : xxx/xxx(the folder must contains .jpg.view file)
     */
    private fun getOtherComicInfo(folderPath: String): ComicBean {
        val chapterFile = File(folderPath)
        val comicBean = ComicBean()
        val chapterBean = ComicBean.ChapterBean()
        val episodeList = ArrayList<ComicBean.ChapterBean.EpisodeBean>()

        for (episodeFile in chapterFile.listFiles()) {
            if (episodeFile.absolutePath.endsWith(".jpg.view")) {
                val episodeBean = ComicBean.ChapterBean.EpisodeBean()
                episodeBean.episodePath = episodeFile.absolutePath
                episodeList.add(episodeBean)
            }
        }
        chapterBean.chapterId = chapterFile.name
        chapterBean.episodeList = episodeList
        val chapterList = ArrayList<ComicBean.ChapterBean>()
        chapterList.add(chapterBean)
        comicBean.chapterList = chapterList

        return comicBean
    }
}