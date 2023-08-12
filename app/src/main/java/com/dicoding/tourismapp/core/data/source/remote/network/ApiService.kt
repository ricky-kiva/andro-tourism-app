package com.dicoding.tourismapp.core.data.source.remote.network

import com.dicoding.tourismapp.core.data.source.remote.response.ListTourismResponse
import io.reactivex.Flowable
import retrofit2.http.GET

interface ApiService {

    @GET("list")
    suspend fun getList(): ListTourismResponse // use suspend to indicate the function could be safely called from coroutine
    // suspend also needed to transform network responses to `Flow`

    /* RxJava
    @GET("list")
    fun getList(): Flowable<ListTourismResponse> // remote data is Flowable*/
}