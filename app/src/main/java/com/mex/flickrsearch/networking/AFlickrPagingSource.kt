package com.mex.flickrsearch.networking

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mex.flickrsearch.data.PhotoResponse

abstract class AFlickrPagingSource : PagingSource<Int, PhotoResponse>() {

    protected var query = ""

    fun setQueryText(queryText:String){
        query = queryText
    }
}