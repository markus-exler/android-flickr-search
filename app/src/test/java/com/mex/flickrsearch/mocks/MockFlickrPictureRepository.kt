package com.mex.flickrsearch.mocks

import androidx.paging.PagingData
import com.mex.flickrsearch.data.IFlickrPictureRepository
import com.mex.flickrsearch.data.model.Picture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockFlickrPictureRepository : IFlickrPictureRepository {

    var returnValue: PagingData<Picture> = PagingData.from(
        listOf(
            Picture(
                id = "44710821501",
                title = "Spanish combat helicopter",
                url = "https://farm2.staticflickr.com/1866/44710821501_579ee86438.jpg"
            )
        )
    )

    override fun getPictureSearchResultStream(query: String): Flow<PagingData<Picture>> {
        return flow {
            emit(returnValue)
        }
    }
}