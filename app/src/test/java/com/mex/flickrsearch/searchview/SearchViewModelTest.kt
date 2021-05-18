package com.mex.flickrsearch.searchview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.mex.flickrsearch.collectDataForTest
import com.mex.flickrsearch.data.model.Picture
import com.mex.flickrsearch.getOrAwaitValue
import com.mex.flickrsearch.mocks.MockFlickrPictureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
class SearchViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var sutSearchViewModel: SearchViewModel
    private lateinit var mockRepository: MockFlickrPictureRepository
    private val mainThreadSurrogate = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        mockRepository = MockFlickrPictureRepository()
        sutSearchViewModel = SearchViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    val mockPictureData = Picture(
        id = "44710821501",
        title = "Spanish combat helicopter",
        url = "https://farm2.staticflickr.com/1866/44710821501_579ee86438.jpg"
    )

    @Test
    fun searchPictures_newValidQuery_currentQueryIsUpdated() {
        mockRepository.returnValue = PagingData.from(
            listOf(
                mockPictureData
            )
        )
        val testQuery = "test"
        sutSearchViewModel.searchPictures(testQuery)
        val actual = sutSearchViewModel.currentQuery.getOrAwaitValue()
        assertEquals(testQuery, actual)
    }

    @Test
    fun searchPictures_newValidQueryWithData_pictureLiveDataIsUpdated() = runBlocking {
        mockRepository.returnValue = PagingData.from(
            listOf(
                mockPictureData
            )
        )
        sutSearchViewModel.searchPictures("test")
        val pagingData = sutSearchViewModel.pictures.getOrAwaitValue()
        val actual = pagingData.collectDataForTest(Dispatchers.Main)
        val expected = listOf(mockPictureData)
        assertEquals(expected, actual)
    }

    @Test
    fun searchPictures_newValidQueryWithoutData_pictureLiveDataIsUpdated() = runBlocking {
        mockRepository.returnValue = PagingData.from(
            emptyList()
        )
        sutSearchViewModel.searchPictures("test")
        val pagingData = sutSearchViewModel.pictures.getOrAwaitValue()
        val actual = pagingData.collectDataForTest(Dispatchers.Main)
        val expected = emptyList<Picture>()
        assertEquals(expected, actual)
    }
}