package com.mex.flickrsearch.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mex.flickrsearch.data.model.Picture
import com.mex.flickrsearch.networking.AFlickrPagingSource
import com.mex.flickrsearch.networking.FlickrPagingSource
import com.mex.flickrsearch.networking.FlickrRestService
import com.mex.flickrsearch.networking.IFlickrApiService
import com.mex.flickrsearch.toPicturePagingDataFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FlickrPictureRepository @Inject constructor(
    private val flickrPagingSource: AFlickrPagingSource) : IFlickrPictureRepository {

    /**
     * Initiates the search and returns the results using [PagingData]
     */
    override fun getPictureSearchResultStream(query: String): Flow<PagingData<Picture>> {
        // init the pager using the paging source and the given query
        flickrPagingSource.setQueryText(query)
        return Pager(
            config = PagingConfig(
                pageSize = REST_REQUEST_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { flickrPagingSource }
        ).flow.toPicturePagingDataFlow()
    }

    companion object {
        /**
         * Number of pictures each rest request should contain per page
         */
        const val REST_REQUEST_PAGE_SIZE = 30
    }
}