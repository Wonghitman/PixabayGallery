package com.vam.android.pagergallery.network.repository

import com.vam.android.pagergallery.network.RequestBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

class AppRepository <T>(private val apiService: T) {

    fun <R> getData(apiCall: suspend T.() -> Response<R>): Flow<RequestBuilder.APIResponse<R>> {
        return flow {
            emit(RequestBuilder.APIResponse.Loading)
            try {
                val response: Response<R> = apiService.apiCall()
                if (response.isSuccessful) {
                    response.body()?.let {
                        emit(RequestBuilder.APIResponse.Success(it))
                    } ?: emit(RequestBuilder.APIResponse.Error("Response body is null"))
                } else {
                    emit(RequestBuilder.APIResponse.Error("Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                emit(RequestBuilder.APIResponse.Error(e.message ?: "Unknown error"))
            }
        }.flowOn(Dispatchers.IO)
    }

}