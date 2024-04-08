package com.vam.android.pagergallery.network.api

import com.vam.android.pagergallery.base.BaseApplication
import com.vam.android.pagergallery.network.bean.Pixabay
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PhotoApi {
    @GET("?key=${BaseApplication.TOKEN}&image_type=photo")
    suspend fun getPhotos(@Query("q") keywords: String,@Query("page") page: Int,@Query("safesearch")searchesarch:Boolean): Response<Pixabay>

}