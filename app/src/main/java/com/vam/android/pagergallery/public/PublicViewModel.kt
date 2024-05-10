package com.vam.android.pagergallery.public

import PhotoPagingSource
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vam.android.pagergallery.network.RequestBuilder
import com.vam.android.pagergallery.network.api.PhotoApi
import com.vam.android.pagergallery.network.bean.Pixabay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PublicViewModel(application: Application): AndroidViewModel(application) {
    private val photoApi = RequestBuilder.getInstance().getAPI(PhotoApi::class.java)
    private var currentQueryValue: String? = null
    val currentSearchResult = MutableLiveData<String>()
    private var photoPagingSource: PhotoPagingSource? = null
    var sharedFlow: SharedFlow<PagingData<Pixabay.PhotoItem>> = MutableSharedFlow()
    private val _pagingData = MutableLiveData<PagingData<Pixabay.PhotoItem>>()
    val pagingData: LiveData<PagingData<Pixabay.PhotoItem>> = _pagingData
    val PagingList: MutableLiveData<List<Pixabay.PhotoItem>> = MutableLiveData()
    fun fetchPhotos(query: String) {

        if (photoPagingSource == null || currentQueryValue != query) {
            photoPagingSource = PhotoPagingSource(photoApi, query)
        }
        currentQueryValue = query
        val newResult: Flow<PagingData<Pixabay.PhotoItem>> = Pager(
            config = PagingConfig(
                pageSize = 20,
                maxSize = Int.MAX_VALUE,//BUG修复
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PhotoPagingSource(photoApi, query) }
        ).flow.cachedIn(viewModelScope)

        viewModelScope.launch {
            newResult.collectLatest { pagingData ->
                _pagingData.value = pagingData
            }
        }


    }

}