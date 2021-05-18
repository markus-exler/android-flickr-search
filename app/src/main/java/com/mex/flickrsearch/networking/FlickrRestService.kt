package com.mex.flickrsearch.networking

import com.mex.flickrsearch.BuildConfig.FLICKR_API_KEY
import com.mex.flickrsearch.data.FlickrResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface FlickrRestService : IFlickrApiService {

    @GET("?method=flickr.photos.search&format=json&nojsoncallback=1&api_key=$FLICKR_API_KEY")
    override suspend fun getSearchedImages(
        @Query("text") query: String,
        @Query("page") page: Int,
        @Query("per_page") itemsPerPage: Int
    ): FlickrResponse
}