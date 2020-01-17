package com.xyoye.comictools.utils

import com.xyoye.comictools.bean.ComicNetworkInfo
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Created by xyoye on 2020/1/16.
 */

object NetworkUtils {

    fun getComicInfo(comicId: String, callback: NetworkCallback) {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null

        try {
            val url = URL("https://manga.bilibili.com/twirp/comic.v2.Comic/ComicDetail")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("user-agent", "Mozilla/5.0 BiliComic/1.9.0")
            connection.setRequestProperty("Host", "manga.bilibili.com")
            connection.setRequestProperty("cache-control", "no-cache")

            connection.connect()
            val dataOutputStream = DataOutputStream(connection.outputStream)

            val params = "device=" + URLEncoder.encode(
                "android", Charsets.UTF_8.toString()
            ) + "&comic_id=" + URLEncoder.encode(comicId, Charsets.UTF_8.toString()).toString()

            dataOutputStream.writeBytes(params)
            dataOutputStream.flush()
            dataOutputStream.close()

            if (connection.responseCode == 200) {
                val inputStream = connection.inputStream
                reader = BufferedReader(InputStreamReader(inputStream))
                val responseText = reader.use(BufferedReader::readText)
                val response = StringBuilder(responseText)
                println(response.toString())
                val comicInfo =
                    Utils.fromJson(response.toString(), ComicNetworkInfo::class.java)
                if (comicInfo?.code == 0) {
                    callback.onSuccess(comicInfo)
                } else {
                    callback.onFailed()
                }
            } else {
                callback.onFailed()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFailed()
        } finally {
            reader?.let {
                try {
                    it.close()
                } catch (ignore: Exception) {

                }
            }

            connection?.disconnect()
        }
    }
}
