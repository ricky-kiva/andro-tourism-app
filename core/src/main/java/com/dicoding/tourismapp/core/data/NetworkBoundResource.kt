package com.dicoding.tourismapp.core.data

import com.dicoding.tourismapp.core.data.source.remote.network.ApiResponse
import com.dicoding.tourismapp.core.utils.AppExecutors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

abstract class NetworkBoundResource<ResultType, RequestType>(private val mExecutors: AppExecutors) {

    private var result: Flow<Resource<ResultType>> = flow {
        emit(Resource.Loading())
        val dbSource = loadFromDB().first() // get only first data flow from loadFromDB
        if (shouldFetch(dbSource)) {
            emit(Resource.Loading())
            when (val apiResponse = createCall().first()) { // assign first data from createCall() as apiResponse
                is ApiResponse.Success -> {
                    saveCallResult(apiResponse.data)
                    emitAll(loadFromDB().map { Resource.Success(it) }) // emit all items of DB
                }
                is  ApiResponse.Empty -> {
                    emitAll(loadFromDB().map { Resource.Success(it)})
                }
                is ApiResponse.Error -> {
                    onFetchFailed()
                    emit(Resource.Error<ResultType>(apiResponse.errorMessage))
                }
            }
        } else {
            emitAll(loadFromDB().map { Resource.Success(it) })
        }
    }

    protected open fun onFetchFailed() {}

    // `abstract functions` is being a template when the `abstract class` is called
    protected abstract fun loadFromDB(): Flow<ResultType>

    protected abstract fun shouldFetch(data: ResultType?): Boolean

    protected abstract suspend fun createCall(): Flow<ApiResponse<RequestType>>

    protected abstract suspend fun saveCallResult(data: RequestType)

    fun asFlow(): Flow<Resource<ResultType>> = result

    /* RxJava
    private val result = PublishSubject.create<Resource<ResultType>>()
    private val mCompositeDisposable = CompositeDisposable()

    init {
        @Suppress("LeakingThis")
        val dbSource = loadFromDB()
        val db = dbSource
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .subscribe { value ->
                dbSource.unsubscribeOn(Schedulers.io()) // GPT says it's not necessary because it will finish its work after subscribe is done
                if (shouldFetch(value)) {
                    fetchFromNetwork()
                } else {
                    result.onNext(Resource.Success(value))
                }
            }
        mCompositeDisposable.add(db)
    }

    // `abstract functions` is being a template when the `abstract class` is called
    protected abstract fun loadFromDB(): Flowable<ResultType>

    protected abstract fun shouldFetch(data: ResultType?): Boolean

    protected abstract fun createCall(): Flowable<ApiResponse<RequestType>>

    protected abstract fun saveCallResult(data: RequestType)

    @SuppressLint("CheckResult")
    private fun fetchFromNetwork() {

        val apiResponse = createCall()

        result.onNext(Resource.Loading(null))
        val response = apiResponse
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .doOnComplete {
                mCompositeDisposable.dispose()
            }
            .subscribe { response ->
                when(response) {
                    is ApiResponse.Success -> {
                        saveCallResult(response.data)
                        val dbSource = loadFromDB()
                        dbSource
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .take(1)
                            .subscribe {
                                dbSource.unsubscribeOn(Schedulers.io()) // GPT says it's not necessary because it will finish its work after subscribe is done
                                result.onNext(Resource.Success(it))
                            }
                    }
                    is ApiResponse.Empty -> {
                        val dbSource = loadFromDB()
                        dbSource
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .take(1)
                            .subscribe {
                                dbSource.unsubscribeOn(Schedulers.io()) // GPT says it's not necessary because it will finish its work after subscribe is done
                                result.onNext(Resource.Success(it))
                            }
                    }
                    is ApiResponse.Error -> {
                        onFetchFailed()
                        result.onNext(Resource.Error(response.errorMessage, null))
                    }
                }
            }
        mCompositeDisposable.add(response)
    }

    fun asFlowable(): Flowable<Resource<ResultType>> = result.toFlowable(BackpressureStrategy.BUFFER)*/
}