package com.mex.flickrsearch.networking

import com.mex.flickrsearch.data.FlickrResponse

interface IFlickrApiService {

    suspend fun getSearchedImages(
        query: String,
        page: Int,
        itemsPerPage: Int
    ): FlickrResponse
}