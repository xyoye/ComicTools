package com.xyoye.comictools.file

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.annotation.LayoutRes
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.xyoye.comictools.R
import kotlinx.android.synthetic.main.dialog_file_manager.*
import java.io.File
import java.text.Collator
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by xyoye on 2020/1/14.
 */

class FileManagerDialog(
    mContext: Context,
    private val callback: FileManagerCallback
) : Dialog(mContext, R.style.Dialog) {
    companion object {
        public var COMIC_CACHE_PATH =
            Environment.getExternalStorageDirectory().absolutePath + "/Download"
    }

    private val fileList = ArrayList<FileManagerBean>()
    private var fileAdapter: FileAdapter? = null
    private var rootPath = Environment.getExternalStorageDirectory().absolutePath

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_file_manager)

        title_tv.text = "请选择文件夹"

        val comicCacheFile = File(COMIC_CACHE_PATH)
        val comicCachePath = if (comicCacheFile.exists() && comicCacheFile.isDirectory)
            comicCacheFile.absolutePath
        else
            rootPath

        default_tv.setOnClickListener { listFolder(File(comicCachePath)) }

        cancel_tv.setOnClickListener { cancel() }

        confirm_tv.setOnClickListener {
            callback.onSelected(path_tv.text.toString())
            dismiss()
        }

        fileAdapter = FileAdapter(R.layout.item_file_manager, fileList)
        fileAdapter?.setOnItemChildClickListener { _, _, position ->
            if (fileList[position].isParent) {
                val parentFile = fileList[position].file.parentFile
                if (parentFile != null && parentFile.exists())
                    listFolder(parentFile)
            } else {
                listFolder(fileList[position].file)
            }
        }

        file_rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        file_rv.adapter = fileAdapter

        listFolder(File(comicCachePath))
    }

    private fun listFolder(parentFile: File) {
        path_tv.text = parentFile.absolutePath
        default_tv.setTextColor(
            ContextCompat.getColor(
                context,
                if (parentFile.absolutePath == COMIC_CACHE_PATH) R.color.text_gray else R.color.text_black
            )
        )

        val childFileList = ArrayList<FileManagerBean>()
        val childFiles = parentFile.listFiles()
        if (childFiles != null){
            for (file: File in parentFile.listFiles()!!) {
                if (file.isDirectory && !file.name.startsWith(".")) {
                    childFileList.add(
                        FileManagerBean(
                            file,
                            file.name,
                            false
                        )
                    )
                }
            }
        }

        childFileList.sortWith(Comparator { o1, o2 ->
            Collator.getInstance(Locale.CHINESE).compare(o1.name, o2.name)
        })

        if (rootPath != parentFile.absolutePath)
            childFileList.add(
                0,
                FileManagerBean(parentFile, "..", true)
            )

        fileList.clear()
        fileList.addAll(childFileList)
        fileAdapter?.notifyDataSetChanged()
    }

    class FileAdapter(@LayoutRes layoutResId: Int, @Nullable data: List<FileManagerBean>) :
        BaseQuickAdapter<FileManagerBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder?, item: FileManagerBean?) {
            helper!!.addOnClickListener(R.id.item_layout)
                .setText(R.id.file_name_tv, item!!.name)
                .setImageResource(
                    R.id.file_type_iv,
                    if (item.isParent)
                        R.drawable.ic_chevron_left_dark
                    else
                        R.drawable.ic_folder_dark
                )
        }

    }

    interface FileManagerCallback {
        fun onSelected(resultPath: String)
    }
}