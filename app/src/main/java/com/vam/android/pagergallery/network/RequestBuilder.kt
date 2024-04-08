package com.vam.android.pagergallery.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RequestBuilder private  constructor(){

    companion object {
        @Volatile
        private var INSTANCE: RequestBuilder? = null

        fun getInstance(): RequestBuilder {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RequestBuilder().also { INSTANCE = it }
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    private val retrofitBuilder: Retrofit = Retrofit.Builder()
        .baseUrl("https://pixabay.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()


    fun <T> getAPI(apiType: Class<T>): T = retrofitBuilder.create(apiType)

    sealed class APIResponse<out T> {
        data class Success<out T>(val data: T) : APIResponse<T>()
        data class Error(val errMsg: String) : APIResponse<Nothing>()
        data object Loading : APIResponse<Nothing>()
    }

}