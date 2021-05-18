package com.mex.flickrsearch.networking

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mex.flickrsearch.data.FlickrPictureRepository
import com.mex.flickrsearch.data.FlickrResponse
import com.mex.flickrsearch.data.FlickrResponseData
import com.mex.flickrsearch.data.PhotoResponse
import com.mex.flickrsearch.mocks.MockFlickrApiService
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FlickrPagingSourceTest {

    private val mockResponseSuccess = FlickrResponse(
        FlickrResponseData(
            1,
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
        ),
        "ok"
    )

    private lateinit var sutFlickrPagingSource: FlickrPagingSource
    private lateinit var mockFlickrApiService: MockFlickrApiService

    @Before
    fun setUp() {
        mockFlickrApiService = MockFlickrApiService()
        sutFlickrPagingSource = FlickrPagingSource(mockFlickrApiService)
    }

    @Test
    fun load_successfulApiRequestInitialFetchOrRefresh_returnCorrectDataInPage() = runBlocking {

        mockFlickrApiService.searchImageReturnValue = mockResponseSuccess

        val actualPage = sutFlickrPagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 50,
                placeholdersEnabled = false
            )
        )

        val expectedPage = PagingSource.LoadResult.Page(
            data = mockResponseSuccess.data.photos,
            prevKey = null,
            nextKey = 1 + (50 / FlickrPictureRepository.REST_REQUEST_PAGE_SIZE)
        )
        assertEquals(expectedPage, actualPage)
    }

    @Test
    fun load_successfulApiRequestOnAppendPage_incrementPageNumberCorrectly() = runBlocking {

        mockFlickrApiService.searchImageReturnValue = mockResponseSuccess

        val actualPage = sutFlickrPagingSource.load(
            PagingSource.LoadParams.Append(
                key = 2,
                loadSize = 50,
                placeholdersEnabled = false
            )
        )

        val expectedPage = PagingSource.LoadResult.Page(
            data = mockResponseSuccess.data.photos,
            prevKey = 1,
            nextKey = 2 + (50 / FlickrPictureRepository.REST_REQUEST_PAGE_SIZE)
        )
        assertEquals(expectedPage, actualPage)
    }

    @Test
    fun load_failedApiRequestWrongApiKey_returnErrorPage() = runBlocking {
        val mockResponse = FlickrResponse(
            status = "fail",
            code = 100,
            message = "Invalid API Key (Key not found)"
        )
        mockFlickrApiService.searchImageReturnValue = mockResponse

        val actualPage = sutFlickrPagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 50,
                placeholdersEnabled = false
            )
        )

        val expectedPage: PagingSource.LoadResult<Int, PhotoResponse> =
            PagingSource.LoadResult.Error(Exception(mockResponse.message))

        assertEquals(expectedPage.toString(), actualPage.toString())
    }

    @Test
    fun load_failedApiRequestHttpException_returnErrorPage() = runBlocking {

        val errorResponse =
            "{\n" +
                    "  \"code\": 502,\n" +
                    "} "
        val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
        val mockResponse = Response.error<String>(502, errorResponseBody)
        mockFlickrApiService.exceptionToThrow = HttpException(mockResponse)
        val actualPage = sutFlickrPagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 50,
                placeholdersEnabled = false
            )
        )

        val expectedPage: PagingSource.LoadResult<Int, PhotoResponse> =
            PagingSource.LoadResult.Error(HttpException(mockResponse))

        assertEquals(expectedPage.toString(), actualPage.toString())
    }

    @Test
    fun load_failedApiRequestIOException_returnErrorPage() = runBlocking {

        val ioExceptionMessage = "TestIOException"
        mockFlickrApiService.exceptionToThrow = IOException(ioExceptionMessage)
        val actualPage = sutFlickrPagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 50,
                placeholdersEnabled = false
            )
        )

        val expectedPage: PagingSource.LoadResult<Int, PhotoResponse> =
            PagingSource.LoadResult.Error(IOException(ioExceptionMessage))

        assertEquals(expectedPage.toString(), actualPage.toString())
    }

    @Test
    fun getRefreshKey_nextKeyNull_returnLastPage(){
        val photo = mockResponseSuccess.data.photos[0]
        val prevKey = 1
        val testPage = PagingSource.LoadResult.Page(
            data = listOf(photo,photo,photo,photo),
            prevKey = prevKey,
            nextKey = null
        )
        val pagingState = PagingState<Int,PhotoResponse>(
            pages = listOf(testPage),
            anchorPosition = 1,
            config = PagingConfig(
                pageSize = FlickrPictureRepository.REST_REQUEST_PAGE_SIZE,
                enablePlaceholders = false
            ),
            leadingPlaceholderCount = 0)

        val actual = sutFlickrPagingSource.getRefreshKey(pagingState)
        assertEquals(prevKey+1,actual)
    }

    @Test
    fun getRefreshKey_prevKeyNull_returnFirstPage(){
        val photo = mockResponseSuccess.data.photos[0]
        val nextKey = 2
        val testPage = PagingSource.LoadResult.Page(
            data = listOf(photo,photo,photo,photo),
            prevKey = null,
            nextKey = nextKey
        )
        val pagingState = PagingState<Int,PhotoResponse>(
            pages = listOf(testPage),
            anchorPosition = 1,
            config = PagingConfig(
                pageSize = FlickrPictureRepository.REST_REQUEST_PAGE_SIZE,
                enablePlaceholders = false
            ),
            leadingPlaceholderCount = 0)

        val actual = sutFlickrPagingSource.getRefreshKey(pagingState)
        assertEquals(nextKey-1,actual)
    }

    @Test
    fun getRefreshKey_anchorNull_returnNull(){
        val photo = mockResponseSuccess.data.photos[0]
        val testPage = PagingSource.LoadResult.Page(
            data = listOf(photo,photo,photo,photo),
            prevKey = null,
            nextKey = 2
        )
        val pagingState = PagingState<Int,PhotoResponse>(
            pages = listOf(testPage),
            anchorPosition = null,
            config = PagingConfig(
                pageSize = FlickrPictureRepository.REST_REQUEST_PAGE_SIZE,
                enablePlaceholders = false
            ),
            leadingPlaceholderCount = 0)

        val actual = sutFlickrPagingSource.getRefreshKey(pagingState)
        assertEquals(null,actual)
    }
}