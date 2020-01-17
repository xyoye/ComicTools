package com.xyoye.comictools.utils

import com.xyoye.comictools.bean.IndexBean
import java.io.*
import java.util.zip.ZipInputStream
import kotlin.experimental.xor

/**
 * Created by xyoye on 2020/1/15.
 */

object IndexUtils {
    fun getIndexInfo(
        indexFilePath: String,
        comicId: String,
        chapterId: String
    ): IndexBean? {
        var fileInputStream: FileInputStream? = null
        var byteArrayInputStream: ByteArrayInputStream? = null
        var bufferedInputStream: BufferedInputStream? = null
        var zipInputStream: ZipInputStream? = null
        try {
            //获取index数据
            fileInputStream = FileInputStream(File(indexFilePath))
            val indexData = ByteArray(fileInputStream.available() - 9)
            fileInputStream.skip(9)
            fileInputStream.read(indexData)

            //获取解密后数据
            val contentData = decryptData(getKeyData(comicId, chapterId), indexData)

            //提取json信息
            byteArrayInputStream = ByteArrayInputStream(contentData)
            bufferedInputStream = BufferedInputStream(byteArrayInputStream)
            zipInputStream = ZipInputStream(bufferedInputStream)
            var zipEntry = zipInputStream.nextEntry
            while (zipEntry != null) {
                if ("index.dat" == zipEntry.name) {
                    val indexJson = getJsonData(zipInputStream)
                    return JsonUtils.fromJson(indexJson, IndexBean::class.java)
                } else {
                    zipEntry = zipInputStream.nextEntry
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileInputStream?.close()
            byteArrayInputStream?.close()
            bufferedInputStream?.close()
            zipInputStream?.close()
        }
        return null
    }

    private fun decryptData(keyData: ByteArray, indexData: ByteArray): ByteArray {
        val decryptData = ByteArray(indexData.size)
        for (i in indexData.indices) {
            decryptData[i] = indexData[i] xor keyData[i % 8]
        }
        return decryptData
    }

    private fun getKeyData(comicId: String, chapterId: String): ByteArray {
        val keyData = ByteArray(8)
        val comicIdInt = comicId.toInt()
        val chapterIdInt = chapterId.toInt()

        keyData[0] = chapterIdInt.toByte()
        keyData[1] = chapterIdInt.shr(8).toByte()
        keyData[2] = chapterIdInt.shr(16).toByte()
        keyData[3] = chapterIdInt.shr(24).toByte()
        keyData[4] = comicIdInt.toByte()
        keyData[5] = comicIdInt.shr(8).toByte()
        keyData[6] = comicIdInt.shr(16).toByte()
        keyData[7] = comicIdInt.shr(24).toByte()

        for (i in keyData.indices) {
            keyData[i] = (keyData[i] % 256).toByte()
        }

        return keyData
    }

    private fun getJsonData(zipInputStream: ZipInputStream): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val dataArray = ByteArray(1024)

        while (true) {
            try {
                val read = zipInputStream.read(dataArray)
                if (read != -1) {
                    byteArrayOutputStream.write(dataArray, 0, read)
                } else {
                    val jsonData = byteArrayOutputStream.toByteArray()
                    try {
                        byteArrayOutputStream.close()
                    } catch (ignore: IOException) {
                    }
                    return String(jsonData)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }

        }
    }
}