package com.vam.android.pagergallery.base

import androidx.lifecycle.ViewModel
import com.vam.android.pagergallery.network.RequestBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

abstract class BaseViewModel() : ViewModel() {

    protected fun <T> CoroutineScope.netLaunch(
        before: () -> Unit = {},
        async: suspend () -> Flow<RequestBuilder.APIResponse<T>>,
        success: (T) -> Unit = {},
        fail: (String) -> Unit = {}
    ) : Job {
        return this.launch {
            before()
            async().onStart {
                // Handle loading state
            }.collect { response ->
                when (response) {
                    is RequestBuilder.APIResponse.Success -> {
                        success(response.data)
                    }

                    is RequestBuilder.APIResponse.Error -> {
                        fail(response.errMsg)
                    }

                    else -> {}
                }
            }
        }
    }
}