package com.mex.flickrsearch.util

import androidx.paging.PagingData
import com.mex.flickrsearch.collectDataForTest
import com.mex.flickrsearch.data.FlickrResponse
import com.mex.flickrsearch.data.PhotoResponse
import com.mex.flickrsearch.data.model.Picture
import com.mex.flickrsearch.isOk
import com.mex.flickrsearch.toPicturePagingDataFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExtensionsTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }


    @Test
    fun isOk_responseOk_returnsTrue() {
        val sutResponse = FlickrResponse(status = "ok")
        val actual = sutResponse.isOk
        assertTrue(actual)
    }

    @Test
    fun isOk_responseFail_returnsFalse() {
        val sutResponse = FlickrResponse(status = "fail")
        val actual = sutResponse.isOk
        assertFalse(actual)
    }

    @Test
    fun toPicture_correctPhotoResponseInput_correctConversionToPicture() = runBlocking {

        val sutFlowPdPicture: Flow<PagingData<Picture>> = flow {
            emit(
                PagingData.from(
                    listOf(
                        PhotoResponse(
                            id = "44710821501",
                            owner = "106680245@N07",
                            secret = "579ee86438",
                            server = "1866",
                            farm = 2,
                            title = "Spanish combat helicopter"
                        )
                    )
                )
            )
        }.toPicturePagingDataFlow()

        val pagingData = sutFlowPdPicture.take(1).toList().first()
        val actual = pagingData.collectDataForTest(Dispatchers.Main)
        val expected = listOf(
            Picture(
                id = "44710821501",
                title = "Spanish combat helicopter",
                url = "https://farm2.staticflickr.com/1866/44710821501_579ee86438.jpg"
            )
        )
        assertEquals(expected, actual)
    }
}