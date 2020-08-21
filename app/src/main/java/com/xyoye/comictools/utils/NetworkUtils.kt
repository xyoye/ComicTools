package com.xyoye.comictools.utils

import com.xyoye.comictools.bean.ComicNetworkInfo

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by xyoye on 2020/1/16.
 */

interface ApiService {
    @Headers(value = [
        "content-type: application/x-www-form-urlencoded;charset=utf-8",
        "user-agent: Mozilla/5.0 BiliComic/3.2.1",
        "Host: manga.bilibili.com"
    ])
    @FormUrlEncoded
    @POST("twirp/comic.v1.Comic/ComicDetail")
    fun getComicDetails(
        @Field("comic_id") comicId: String,
        @Field("device") device: String = "android",
        @Field("platform") platform: String = "android"
    ): Call<ComicNetworkInfo>
}

object NetworkUtils {
    fun getComicInfo(comicId: String, callback: NetworkCallback) {
        Retrofit.Builder()
            .baseUrl("https://manga.bilibili.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
            .getComicDetails(comicId).execute().apply {
                if (isSuccessful) {
                    body()?.run {
                        callback.onSuccess(this)
                        return
                    }
                }
                callback.onFailed()
            }
    }
}
