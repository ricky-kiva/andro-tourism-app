package com.dicoding.tourismapp.core.domain.usecase

import com.dicoding.tourismapp.core.data.Resource
import com.dicoding.tourismapp.core.domain.model.Tourism
import com.dicoding.tourismapp.core.domain.repository.ITourismRepository
import io.reactivex.Flowable
import kotlinx.coroutines.flow.Flow

class TourismInteractor(private val tourismRepository: ITourismRepository): TourismUseCase {

    override fun getAllTourism(): Flow<Resource<List<Tourism>>> =
        tourismRepository.getAllTourism()

    override fun getFavoriteTourism(): Flow<List<Tourism>> =
        tourismRepository.getFavoriteTourism()

    /* RxJava
    override fun getAllTourism(): Flowable<Resource<List<Tourism>>> =
        tourismRepository.getAllTourism()

    override fun getFavoriteTourism(): Flowable<List<Tourism>> =
        tourismRepository.getFavoriteTourism()*/

    override fun setFavoriteTourism(tourism: Tourism, state: Boolean) {
        tourismRepository.setFavoriteTourism(tourism, state)
    }

}