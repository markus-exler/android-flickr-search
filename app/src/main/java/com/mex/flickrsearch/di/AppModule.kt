package com.mex.flickrsearch.di

import com.mex.flickrsearch.data.FlickrPictureRepository
import com.mex.flickrsearch.data.IFlickrPictureRepository
import com.mex.flickrsearch.networking.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://api.flickr.com/services/rest/"

    @Singleton
    @Provides
    fun provideFlickrRestService(retrofit: Retrofit): FlickrRestService =
        retrofit.create(FlickrRestService::class.java)

    @Singleton
    @Provides
    fun provideRetrofitWithMoshi(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .build()

    @Singleton
    @Provides
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Singleton
    @Provides
    fun provideOkHttpClient(networkConnectionInterceptor: NetworkConnectionInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(networkConnectionInterceptor)
            .build()

    @Singleton
    @Provides
    fun provideFlickrApiService(flickrRestService: FlickrRestService): IFlickrApiService =
        flickrRestService

    @Singleton
    @Provides
    fun provideFlickrPagingSource(flickrPagingSource: FlickrPagingSource): AFlickrPagingSource =
        flickrPagingSource

    @Singleton
    @Provides
    fun provideFlickrPictureRepository(flickrPictureRepository: FlickrPictureRepository): IFlickrPictureRepository =
        flickrPictureRepository
}