package com.dicoding.tourismapp.home

import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.tourismapp.core.domain.usecase.TourismUseCase

class HomeViewModel(tourismUseCase: TourismUseCase) : ViewModel() {

    val tourism = tourismUseCase.getAllTourism().asLiveData()

    /* RxJava
    // `LiveDataReactiveStreams` convert reactive stream into livedata
    val tourism = LiveDataReactiveStreams.fromPublisher(tourismUseCase.getAllTourism())*/

}