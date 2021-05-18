package com.mex.flickrsearch.data

import com.squareup.moshi.Json

// Rest response wrapper
data class FlickrResponse(
    @Json(name = "photos")
    val data: FlickrResponseData = FlickrResponseData(0, emptyList()),
    @Json(name = "stat")
    val status: String = "",
    val code: Int = 200,
    val message: String = ""
)

// Json data container
data class FlickrResponseData(
    val page: Int,
    @Json(name = "photo")
    val photos: List<PhotoResponse>
)

// json photo class
data class PhotoResponse(
    val id: String,
    val owner: String,
    val secret: String,
    val server: String,
    val farm: Int,
    val title: String
)