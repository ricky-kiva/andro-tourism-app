package com.dicoding.tourismapp.core.data

import com.dicoding.tourismapp.core.data.source.local.LocalDataSource
import com.dicoding.tourismapp.core.data.source.remote.RemoteDataSource
import com.dicoding.tourismapp.core.data.source.remote.network.ApiResponse
import com.dicoding.tourismapp.core.data.source.remote.response.TourismResponse
import com.dicoding.tourismapp.core.domain.model.Tourism
import com.dicoding.tourismapp.core.domain.repository.ITourismRepository
import com.dicoding.tourismapp.core.utils.AppExecutors
import com.dicoding.tourismapp.core.utils.DataMapper
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TourismRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val appExecutors: AppExecutors
): ITourismRepository {

    companion object {
        @Volatile
        private var instance: TourismRepository? = null

        fun getInstance(
            remoteData: RemoteDataSource,
            localData: LocalDataSource,
            appExecutors: AppExecutors
        ): TourismRepository =
            instance ?: synchronized(this) {
                instance ?: TourismRepository(remoteData, localData, appExecutors)
            }
    }

    // Get `Tourism` list from API & local database
    override fun getAllTourism(): Flow<Resource<List<Tourism>>> =

        // object to manage the flow of data betweek network & local storage
        object : NetworkBoundResource<List<Tourism>, List<TourismResponse>>(appExecutors) {

            // called first. checks local database (Room), then map to domain `model`
            override fun loadFromDB(): Flow<List<Tourism>> {
                return localDataSource.getAllTourism().map {
                    DataMapper.mapEntitiesToDomain(it)
                }
            }

            // determine network call is necessary based on data retrieved from local database
            // will check data is null or empty to determine fetch or not
            override fun shouldFetch(data: List<Tourism>?): Boolean =
                data.isNullOrEmpty()

            // if shouldFetch() is true, this function called to get data from remote source
            override suspend fun createCall(): Flow<ApiResponse<List<TourismResponse>>> =
                remoteDataSource.getAllTourism()

            // executed after remote fetch success. map response to entities then add to local source
            override suspend fun saveCallResult(data: List<TourismResponse>) {
                val tourismList = DataMapper.mapResponsesToEntities(data)
                localDataSource.insertTourism(tourismList)
            }

            // NetworkBoundResource diagram: https://d17ivq9b7rppb3.cloudfront.net/original/academy/20200812165119cf5c79dfc223d9f04f39899ddfc29700.png
        }.asFlow()

    // get favorite `tourism` list
    // use `domain` to be `presented`
    override fun getFavoriteTourism(): Flow<List<Tourism>> {
        return localDataSource.getFavoriteTourism().map {
            DataMapper.mapEntitiesToDomain(it)
        }
    }

    /* RxJava
    // Get `Tourism` list from API & local database
    override fun getAllTourism(): Flowable<Resource<List<Tourism>>> =

        // object to manage the flow of data betweek network & local storage
        object : NetworkBoundResource<List<Tourism>, List<TourismResponse>>(appExecutors) {

            // called first. checks local database (Room), then map to domain `model`
            override fun loadFromDB(): Flowable<List<Tourism>> {
                return localDataSource.getAllTourism().map {
                    DataMapper.mapEntitiesToDomain(it)
                }
            }

            // determine network call is necessary based on data retrieved from local database
            // will check data is null or empty to determine fetch or not
            override fun shouldFetch(data: List<Tourism>?): Boolean =
                data.isNullOrEmpty()

            // if shouldFetch() is true, this function called to get data from remote source
            override fun createCall(): Flowable<ApiResponse<List<TourismResponse>>> =
                remoteDataSource.getAllTourism()

            // executed after remote fetch success. map response to entities then add to local source
            override fun saveCallResult(data: List<TourismResponse>) {
                val tourismList = DataMapper.mapResponsesToEntities(data)
                localDataSource.insertTourism(tourismList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()) // omit this so 'UI updates' by `liveData` happened in Main Thread (if not, data not shown)
                    .subscribe()
            }

        // NetworkBoundResource diagram: https://d17ivq9b7rppb3.cloudfront.net/original/academy/20200812165119cf5c79dfc223d9f04f39899ddfc29700.png
        }.asFlowable()

    // get favorite `tourism` list
    // use `domain` to be `presented`
    override fun getFavoriteTourism(): Flowable<List<Tourism>> {
        return localDataSource.getFavoriteTourism().map {
            DataMapper.mapEntitiesToDomain(it)
        }
    }*/

    // set favorite `tourism`
    // use `entity` to be sent to `DAO`
    override fun setFavoriteTourism(tourism: Tourism, state: Boolean) {
        val tourismEntity = DataMapper.mapDomainToEntity(tourism)
        appExecutors.diskIO().execute { localDataSource.setFavoriteTourism(tourismEntity, state) }
    }
}

