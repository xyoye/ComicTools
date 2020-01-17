package com.xyoye.comictools.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by xyoye on 2020/1/17.
 */

object ComicFileUtils {

    /**
     * convert encrypt file to normal webp file
     *
     * just skip 9 byte
     */
    fun convert(inputPath: String, outputPath: String): Boolean {
        var fileInputStream: FileInputStream? = null
        var fileOutputStream: FileOutputStream? = null

        try {
            val outputFile = File(outputPath)
            val inputFile = File(inputPath)

            if (outputFile.exists())
                outputFile.delete()
            outputFile.createNewFile()

            fileInputStream = FileInputStream(inputFile)
            val byteArray = ByteArray(fileInputStream.available() - 9)
            fileInputStream.skip(9)
            fileInputStream.read(byteArray)

            fileOutputStream = FileOutputStream(outputFile)
            fileOutputStream.write(byteArray)
            fileOutputStream.flush()

            return true
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileInputStream?.let {
                try {
                    fileInputStream.close()
                } catch (ignore: IOException) {

                }
            }
            fileOutputStream?.let {
                try {
                    fileOutputStream.close()
                } catch (ignore: IOException) {

                }
            }
        }
        return false
    }
}