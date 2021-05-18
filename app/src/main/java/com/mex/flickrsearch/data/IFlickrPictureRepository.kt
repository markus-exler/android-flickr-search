package com.mex.flickrsearch.data

import androidx.paging.PagingData
import com.mex.flickrsearch.data.model.Picture
import kotlinx.coroutines.flow.Flow

interface IFlickrPictureRepository {
    /**
     * Initiates the search and returns the results using [PagingData]
     */
    fun getPictureSearchResultStream(query: String): Flow<PagingData<Picture>>
}