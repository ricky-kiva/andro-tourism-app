package com.dicoding.tourismapp.core.data.source.remote

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dicoding.tourismapp.core.data.source.remote.network.ApiResponse
import com.dicoding.tourismapp.core.data.source.remote.network.ApiService
import com.dicoding.tourismapp.core.data.source.remote.response.ListTourismResponse
import com.dicoding.tourismapp.core.data.source.remote.response.TourismResponse
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class RemoteDataSource private constructor(private val apiService: ApiService) {
    companion object {
        @Volatile
        private var instance: RemoteDataSource? = null

        fun getInstance(service: ApiService): RemoteDataSource =
            instance ?: synchronized(this) {
                instance ?: RemoteDataSource(service)
            }
    }

    @SuppressLint("CheckResult")
    fun getAllTourism(): Flowable<ApiResponse<List<TourismResponse>>> {
        val resultData = PublishSubject.create<ApiResponse<List<TourismResponse>>>()

        val client = apiService.getList()

        //get data from remote network
        client
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .subscribe({ response ->
                val dataArray = response.places
                resultData.onNext(if (dataArray.isNotEmpty()) ApiResponse.Success(dataArray) else ApiResponse.Empty)
            }, { error ->
                resultData.onNext(ApiResponse.Error(error.message.toString()))
                Log.e("RemoteDataSource", error.toString())
            })

        return resultData.toFlowable(BackpressureStrategy.BUFFER)
    }
}

