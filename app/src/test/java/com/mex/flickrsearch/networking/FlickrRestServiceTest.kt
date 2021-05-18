package com.mex.flickrsearch.networking

import com.mex.flickrsearch.BuildConfig.FLICKR_API_KEY
import com.mex.flickrsearch.MockResponseFromFile
import com.mex.flickrsearch.data.FlickrResponse
import com.mex.flickrsearch.data.FlickrResponseData
import com.mex.flickrsearch.data.PhotoResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import retrofit2.HttpException
import java.io.IOException

@ExperimentalCoroutinesApi
class FlickrRestServiceTest {

    private lateinit var mockWebServer: MockWebServer

    private lateinit var client: OkHttpClient

    private lateinit var moshi: Moshi

    private lateinit var retrofit: Retrofit

    private lateinit var sutFlickrRestService: FlickrRestService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        client = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()

        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url(""))
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        sutFlickrRestService = retrofit.create(FlickrRestService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun getSearchedImages_validRequest_parseValidResponseWithDataCorrectly() {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(MockResponseFromFile("success_response.json").content)
        mockWebServer.enqueue(response)

        runBlocking {
            val actual = sutFlickrRestService.getSearchedImages("testQuery", 1, 1)
            val expected = FlickrResponse(
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
            assertEquals(expected,actual)
        }
    }

    @Test
    fun getSearchedImages_requestWithWrongApiKey_parseErrorResponseCorrectly() {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(MockResponseFromFile("error_response.json").content)
        mockWebServer.enqueue(response)

        runBlocking {
            val actual = sutFlickrRestService.getSearchedImages("testQuery", 1, 1)
            val expected = FlickrResponse(
                status = "fail",
                code = 100,
                message = "Invalid API Key (Key not found)"
            )
            assertEquals(expected,actual)
        }
    }

    @Test
    fun getSearchImages_validRequest_checkCorrectRestRequestPath(){
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(MockResponseFromFile("success_response.json").content)
        mockWebServer.enqueue(response)

        runBlocking {
            sutFlickrRestService.getSearchedImages("testQuery", 5, 50)
            val actualRequest = mockWebServer.takeRequest().path
            val expected = "/?method=flickr.photos.search&format=json&nojsoncallback=1&api_key=$FLICKR_API_KEY"+
                    "&text=testQuery"+
                    "&page=5"+
                    "&per_page=50"
            assertEquals(expected,actualRequest)
        }
    }

    @Test
    fun getSearchImages_validRequestBadGateway_ThrowsHttpException(){
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_BAD_GATEWAY)
        mockWebServer.enqueue(response)
        assertThrows(HttpException::class.java) {
            runBlocking {
                sutFlickrRestService.getSearchedImages("testQuery", 5, 50)
            }
        }

    }

    @Test
    fun getSearchImages_validRequestConnectionTimeOut_ThrowsIOException(){
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(MockResponseFromFile("success_response.json").content)
            .throttleBody(8, 2, TimeUnit.SECONDS)
        mockWebServer.enqueue(response)
        assertThrows(IOException::class.java) {
            runBlocking {
                sutFlickrRestService.getSearchedImages("testQuery", 5, 50)
            }
        }
    }

}