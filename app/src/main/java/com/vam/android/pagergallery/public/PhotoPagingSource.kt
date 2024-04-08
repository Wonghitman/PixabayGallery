import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LoadType
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vam.android.pagergallery.base.BaseApplication
import com.vam.android.pagergallery.network.api.PhotoApi
import com.vam.android.pagergallery.network.bean.Pixabay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PhotoPagingSource(
    private val photoApi: PhotoApi,
    private val query: String,
) : PagingSource<Int, Pixabay.PhotoItem>() {
    private var currentPage = 1

    suspend fun retry(): LoadResult<Int, Pixabay.PhotoItem> {
        // Handle retry logic here
        // For example, you can try to load data again
        return load(LoadParams.Refresh(currentPage, 20, false))
    }

    override suspend fun load( params: LoadParams<Int>): LoadResult<Int, Pixabay.PhotoItem> {
        val position = params.key ?: 1
        currentPage = position
        return try {

            val response = photoApi.getPhotos(query,position,BaseApplication.SAFE_SEARCH)

            Log.d("PhotoPagingSource", "Received response: $response")
            val photos = response.body()?.hits ?: emptyList()

            LoadResult.Page(
                data = photos,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (photos.isEmpty()) null else position + 1
            )
        } catch (exception: IOException) {
            Log.e("PhotoPagingSource", "IOException occurred", exception)

            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            Log.e("PhotoPagingSource", "HttpException occurred", exception)
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Pixabay.PhotoItem>): Int? {
        return state.anchorPosition
    }
}