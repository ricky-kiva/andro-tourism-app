package com.dicoding.tourismapp.core.data.source.remote

import android.util.Log
import com.dicoding.tourismapp.core.data.source.remote.network.ApiResponse
import com.dicoding.tourismapp.core.data.source.remote.network.ApiService
import com.dicoding.tourismapp.core.data.source.remote.response.TourismResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.lang.Exception

class RemoteDataSource(private val apiService: ApiService) {
    companion object {
        @Volatile
        private var instance: RemoteDataSource? = null

        fun getInstance(service: ApiService): RemoteDataSource =
            instance ?: synchronized(this) {
                instance ?: RemoteDataSource(service)
            }
    }

    suspend fun getAllTourism(): Flow<ApiResponse<List<TourismResponse>>> {
        return flow { // build a new `flow`
            try { // response handling using try-catch
                val response = apiService.getList()
                val dataArray = response.places
                if (dataArray.isNotEmpty()) {
                    emit(ApiResponse.Success(response.places))
                } else {
                    emit(ApiResponse.Empty)
                }
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.toString()))
                Log.e("RemoteDataSource", e.toString())
            }
        }.flowOn(Dispatchers.IO) // do `flow`'s work on `Dispatchers.IO`
    }

    /* RxJava
    @SuppressLint("CheckResult")
    fun getAllTourism(): Flowable<ApiResponse<List<TourismResponse>>> {
        // create Subject. it emit everytime there is new data to none/multiple observer (hot stream)
        val resultData = PublishSubject.create<ApiResponse<List<TourismResponse>>>()

        val client = apiService.getList()

        //get data from remote network
        client
            .subscribeOn(Schedulers.computation()) // send data using computation (for high CPU usage)
            .observeOn(AndroidSchedulers.mainThread()) // get data on main thread
            .take(1) // take the first data caught
            .subscribe({ response ->
                val dataArray = response.places
                resultData.onNext(if (dataArray.isNotEmpty()) ApiResponse.Success(dataArray) else ApiResponse.Empty)
            }, { error ->
                resultData.onNext(ApiResponse.Error(error.message.toString()))
                Log.e("RemoteDataSource", error.toString())
            }) // process the caught data

        // convert to Flowable & will buffer the data in memory if consumer slow to process them
        return resultData.toFlowable(BackpressureStrategy.BUFFER)
    }*/
}

