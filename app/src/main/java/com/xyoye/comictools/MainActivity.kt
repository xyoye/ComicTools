package com.xyoye.comictools

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.xyoye.comictools.bean.ComicBean
import com.xyoye.comictools.file.FileManagerDialog
import com.xyoye.comictools.utils.ComicFileUtils
import com.xyoye.comictools.utils.ComicInfoUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private var mComicBean: ComicBean? = null
    private var mOutputPath: String? = null

    @SuppressLint("SetTextI18n")
    private var handler = Handler {
        when (it.what) {
            1001 -> {
                comic_name_tv.text = mComicBean!!.comicName
                chapter_count_tv.text = "共" + mComicBean!!.chapterList!!.size.toString() + "话"

                var fileCount = 0
                for (chapter in mComicBean!!.chapterList!!) {
                    fileCount += chapter.episodeList!!.size
                }
                file_count_tv.text = "共" + fileCount.toString() + "页"

                mOutputPath = FileManagerDialog.COMIC_CACHE_PATH
                output_path_tv.text = mOutputPath

                if (fileCount > 0) {
                    transform_bt.progress(1f)
                    transform_bt.isClickable = true
                    transform_bt.background =
                        ContextCompat.getDrawable(this@MainActivity, R.drawable.background_button_theme)
                } else {
                    mComicBean = null
                }
            }
            1002 -> {
                ToastUtils.showShort("转换完成")
                transform_bt.setLock(false)
            }
            1003 -> {
                transform_bt.progress(it.obj as Float)
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        select_origin_path_tv.setOnClickListener {
            FileManagerDialog(this, object : FileManagerDialog.FileManagerCallback {
                override fun onSelected(resultPath: String) {
                    origin_path_tv.text = resultPath
                    transform_bt.isClickable = false
                    transform_bt.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.background_button_gray
                    )

                    Thread(Runnable {
                        mComicBean = ComicInfoUtils.getComicData(resultPath)
                        if (mComicBean != null) {
                            handler.sendEmptyMessage(1001)
                        }
                    }).start()
                }
            }).show()
        }

        select_output_path_tv.setOnClickListener {
            FileManagerDialog(this, object : FileManagerDialog.FileManagerCallback {
                override fun onSelected(resultPath: String) {
                    mOutputPath = resultPath
                    output_path_tv.text = mOutputPath
                }
            }).show()
        }

        output_path_tv.setOnClickListener {
            val outputPath = output_path_tv.text
            if(!outputPath.isNullOrBlank()){
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("OutPut", outputPath)
                clipboardManager.primaryClip = clipData
                ToastUtils.showShort("输出路径已复制")
            }
        }

        transform_bt.setOnClickListener {
            if (transform_bt.isLocked()){
                ToastUtils.showShort("转换进行中，请稍后")
                return@setOnClickListener
            }
            if (mComicBean == null){
                ToastUtils.showShort("任务为空，无法进行转换")
                return@setOnClickListener
            }
            transform_bt.setLock(true)
            Thread(Runnable {
                val count = doConvert()
                val msg = Message()
                msg.what = 1002
                msg.obj = count
                handler.sendMessage(msg)
            }).start()
        }
    }

    //检查权限
    @SuppressLint("WrongConstant")
    private fun checkPermission() {
        PermissionUtils.permission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).callback(object : PermissionUtils.SimpleCallback {
            override fun onGranted() {

            }

            override fun onDenied() {
                ToastUtils.showShort("获取文件管理权限失败，后续操作将无法进行")
            }
        }).request()
    }

    private fun doConvert(): Int {
        var convertCount = 0

        val outputFolder = File(mOutputPath)
        if (!outputFolder.exists()) {
            outputFolder.mkdirs()
        }

        val comicFolderName = when {
            mComicBean!!.comicId == null -> "未知漫画"
            mComicBean!!.comicId == mComicBean!!.comicName -> mComicBean!!.comicId + "_new"
            else -> mComicBean!!.comicName
        }
        val comicFolder = File(outputFolder, comicFolderName)
        if (!comicFolder.exists())
            comicFolder.mkdir()

        var totalCount = 0
        var durationCount = 0
        for (chapter in mComicBean!!.chapterList!!) {
            totalCount += chapter.episodeList!!.size
        }

        for (chapter in mComicBean!!.chapterList!!) {
            val chapterName =
                if (chapter.chapterNum.isNullOrBlank()) chapter.chapterId else "第" + chapter.chapterNum + "话 " + chapter.chapterName
            val chapterFolder = File(comicFolder, chapterName)
            if (!chapterFolder.exists())
                chapterFolder.mkdir()

            for (index in chapter.episodeList!!.indices) {

                val episode = chapter.episodeList!![index]
                val outputPath = if (episode.episodeIndex == 0) {
                    val fileName = FileUtils.getFileNameNoExtension(episode.episodePath)
                    chapterFolder.absolutePath + "/" + FileUtils.getFileNameNoExtension(fileName) + ".webp"
                } else{
                    chapterFolder.absolutePath + "/" + episode.episodeIndex + ".webp"
                }
                if (ComicFileUtils.convert(episode.episodePath!!, outputPath))
                    convertCount++

                val msg = Message()
                msg.what = 1003
                msg.obj = (++durationCount).toFloat() / totalCount.toFloat()
                handler.sendMessage(msg)
            }
        }

        return convertCount
    }
}
