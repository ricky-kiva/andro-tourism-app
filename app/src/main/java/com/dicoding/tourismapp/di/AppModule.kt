package com.dicoding.tourismapp.di

import com.dicoding.tourismapp.core.domain.usecase.TourismInteractor
import com.dicoding.tourismapp.core.domain.usecase.TourismUseCase
import com.dicoding.tourismapp.detail.DetailTourismViewModel
import com.dicoding.tourismapp.favorite.FavoriteViewModel
import com.dicoding.tourismapp.home.HomeViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val useCaseModule = module {
    factory<TourismUseCase> { TourismInteractor(get()) } // provide `TourismUseCase` to `TourismInteractor` using `single<>`
}

val viewModelModule = module {
    viewModel { HomeViewModel(get()) } // configure ViewModelFactory automatically using `koin-android-viewmodel` library
    viewModel { FavoriteViewModel(get()) }
    viewModel { DetailTourismViewModel(get()) }
}