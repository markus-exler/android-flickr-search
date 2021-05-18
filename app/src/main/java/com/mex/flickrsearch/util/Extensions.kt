package com.mex.flickrsearch

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.paging.PagingData
import androidx.paging.map
import com.mex.flickrsearch.data.FlickrResponse
import com.mex.flickrsearch.data.PhotoResponse
import com.mex.flickrsearch.data.model.Picture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Maps the [PagingData] [Flow] of type [PhotoResponse] to a[PagingData] [Flow] of type [Picture] (domain data)
 */
fun Flow<PagingData<PhotoResponse>>.toPicturePagingDataFlow(): Flow<PagingData<Picture>> =
    this.map { pagingData ->
        pagingData.map {
            Picture(
                it.id,
                it.title,
                url = "https://farm${it.farm}.staticflickr.com/${it.server}/${it.id}_${it.secret}.jpg"
            )
        }
    }

/**
 * Checks whether the [FlickrResponse] is ok, hence the server didn't send a error response without data
 */
val FlickrResponse.isOk: Boolean
    get() = this.status == "ok"

/**
 * Checks if the app has connection to the internet
 */
fun Context.isConnectedToInternet(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        connectivityManager.run {
            activeNetworkInfo?.run {
                when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        } ?: false
    }
}