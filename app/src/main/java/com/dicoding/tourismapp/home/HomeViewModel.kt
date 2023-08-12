package com.dicoding.tourismapp.home

import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import com.dicoding.tourismapp.core.domain.usecase.TourismUseCase

class HomeViewModel(tourismUseCase: TourismUseCase) : ViewModel() {

    // `LiveDataReactiveStreams` convert reactive stream into livedata
    val tourism = LiveDataReactiveStreams.fromPublisher(tourismUseCase.getAllTourism())

}