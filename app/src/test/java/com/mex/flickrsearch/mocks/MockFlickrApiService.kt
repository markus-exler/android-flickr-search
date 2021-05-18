package com.mex.flickrsearch.mocks

import com.mex.flickrsearch.data.FlickrResponse
import com.mex.flickrsearch.networking.IFlickrApiService

class MockFlickrApiService : IFlickrApiService {

    var exceptionToThrow : Exception? = null
    var searchImageReturnValue = FlickrResponse(
        status = "fail",
        code = 100,
        message = "Invalid API Key (Key not found)"
    )

    override suspend fun getSearchedImages(
        query: String,
        page: Int,
        itemsPerPage: Int
    ): FlickrResponse {
        exceptionToThrow?.let {
            throw  it
        } ?: return searchImageReturnValue
    }
}