package com.mex.flickrsearch.networking

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mex.flickrsearch.data.FlickrPictureRepository.Companion.REST_REQUEST_PAGE_SIZE
import com.mex.flickrsearch.data.PhotoResponse
import com.mex.flickrsearch.isOk
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

const val FLICKR_STARTING_PAGE_INDEX = 1

class FlickrPagingSource @Inject constructor(
    private val service: IFlickrApiService) : AFlickrPagingSource() {

    override fun getRefreshKey(state: PagingState<Int, PhotoResponse>): Int? {
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so just return null.

        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PhotoResponse> {
        val position = params.key ?: FLICKR_STARTING_PAGE_INDEX
        return try {
            val response = service.getSearchedImages(query, position, params.loadSize)
            // check if api response is actually ok and therefore contains photo data
            // other HTTP errors are caught by the HTTP exception
            if (response.isOk) {
                val photos = response.data.photos
                val nextKey = if (photos.isEmpty()) {
                    null
                } else {
                    position + (params.loadSize / REST_REQUEST_PAGE_SIZE)
                }
                LoadResult.Page(
                    data = photos,
                    prevKey = if (position == FLICKR_STARTING_PAGE_INDEX) null else position - 1,
                    nextKey = nextKey
                )
            } else {
                LoadResult.Error(Exception(response.message))
            }
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }
}