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
    companion object {
        const val CODE_PREPARED_CONVERT = 1001
        const val CODE_ON_CONVERTING = 1002
        const val CODE_AFTER_CONVERTED = 1003
    }

    private var mComicBean: ComicBean? = null
    private var mOutputPath: String? = null

    @SuppressLint("SetTextI18n")
    private var handler = Handler {
        when (it.what) {
            CODE_PREPARED_CONVERT -> {
                //show comic name
                comic_name_tv.text = mComicBean!!.comicName
                //show chapter count
                chapter_count_tv.text = "共" + mComicBean!!.chapterList!!.size.toString() + "话"

                //show total page count
                var fileCount = 0
                for (chapter in mComicBean!!.chapterList!!) {
                    fileCount += chapter.episodeList!!.size
                }
                file_count_tv.text = "共" + fileCount.toString() + "页"

                //default output path is BiliBili comic cache path
                mOutputPath = FileManagerDialog.COMIC_CACHE_PATH
                output_path_tv.text = mOutputPath


                //must page count > 0 can do convert
                if (fileCount > 0) {
                    convert_bt.progress(1f)
                    convert_bt.isClickable = true
                    convert_bt.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.background_button_theme
                    )
                } else {
                    mComicBean = null
                }
            }
            CODE_ON_CONVERTING -> {
                ToastUtils.showShort("转换完成")
                convert_bt.setLock(false)
            }
            CODE_AFTER_CONVERTED -> {
                //update convert duration
                convert_bt.progress(it.obj as Float)
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        //select input comic folder
        select_origin_path_tv.setOnClickListener {
            FileManagerDialog(this, object : FileManagerDialog.FileManagerCallback {
                override fun onSelected(resultPath: String) {
                    //can't click button before conversion prepared
                    origin_path_tv.text = resultPath
                    convert_bt.isClickable = false
                    convert_bt.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.background_button_gray
                    )

                    Thread(Runnable {
                        mComicBean = ComicInfoUtils.getComicData(resultPath)
                        if (mComicBean != null) {
                            handler.sendEmptyMessage(CODE_PREPARED_CONVERT)
                        }
                    }).start()
                }
            }).show()
        }

        //select output folder
        select_output_path_tv.setOnClickListener {
            FileManagerDialog(this, object : FileManagerDialog.FileManagerCallback {
                override fun onSelected(resultPath: String) {
                    mOutputPath = resultPath
                    output_path_tv.text = mOutputPath
                }
            }).show()
        }

        //copy output path
        output_path_tv.setOnClickListener {
            val outputPath = output_path_tv.text
            if (!outputPath.isNullOrBlank()) {
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("OutPut", outputPath)
                clipboardManager.setPrimaryClip(clipData)
                ToastUtils.showShort("输出路径已复制")
            }
        }

        //do convert
        convert_bt.setOnClickListener {
            if (convert_bt.isLocked()) {
                ToastUtils.showShort("转换进行中，请稍后")
                return@setOnClickListener
            }
            if (mComicBean == null) {
                ToastUtils.showShort("任务为空，无法进行转换")
                return@setOnClickListener
            }
            convert_bt.setLock(true)
            Thread(Runnable {
                val count = doConvert()
                val msg = Message()
                msg.what = CODE_ON_CONVERTING
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

        //check output folder
        val outputFolder = File(mOutputPath)
        if (!outputFolder.exists()) {
            outputFolder.mkdirs()
        }

        //comic folder name
        val comicFolderName = when {
            mComicBean!!.comicId == null -> "未知漫画"
            mComicBean!!.comicId == mComicBean!!.comicName -> mComicBean!!.comicId + "_new"
            else -> mComicBean!!.comicName
        }
        val comicFolder = File(outputFolder, comicFolderName)
        if (!comicFolder.exists())
            comicFolder.mkdir()

        //calc total page count
        var totalCount = 0
        var durationCount = 0
        for (chapter in mComicBean!!.chapterList!!) {
            totalCount += chapter.episodeList!!.size
        }

        //ergodic comic bean
        for (chapter in mComicBean!!.chapterList!!) {
            //chapter folder
            val chapterName =
                if (chapter.chapterNum.isNullOrBlank()) chapter.chapterId else "${chapter.chapterNum} ${chapter.chapterName}"
            val chapterFolder = File(comicFolder, chapterName)
            if (!chapterFolder.exists())
                chapterFolder.mkdir()

            for (index in chapter.episodeList!!.indices) {
                //convert page file
                val episode = chapter.episodeList!![index]
                val outputPath = if (episode.episodeIndex == 0) {
                    val fileName = FileUtils.getFileNameNoExtension(episode.episodePath)
                    chapterFolder.absolutePath + "/" + FileUtils.getFileNameNoExtension(fileName) + ".webp"
                } else {
                    chapterFolder.absolutePath + "/" + episode.episodeIndex + ".webp"
                }
                if (ComicFileUtils.convert(episode.episodePath!!, outputPath))
                    convertCount++

                //update progress button
                val msg = Message()
                msg.what = CODE_AFTER_CONVERTED
                msg.obj = (++durationCount).toFloat() / totalCount.toFloat()
                handler.sendMessage(msg)
            }
        }

        return convertCount
    }
}
