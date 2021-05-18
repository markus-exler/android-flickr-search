package com.mex.flickrsearch.networking

import android.content.Context
import com.mex.flickrsearch.R
import com.mex.flickrsearch.isConnectedToInternet
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * OkHttp interceptor to check for network connection before performing REST request
 */
class NetworkConnectionInterceptor @Inject constructor(
    @ApplicationContext val context: Context)
    : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!context.isConnectedToInternet()) {
            throw IOException(context.getString(R.string.no_internet))
        }
        val builder = chain.request().newBuilder()
        return chain.proceed(builder.build())
    }
}