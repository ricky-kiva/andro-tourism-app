package com.dicoding.tourismapp.core.di

import androidx.room.Room
import com.dicoding.tourismapp.core.data.TourismRepository
import com.dicoding.tourismapp.core.data.source.local.LocalDataSource
import com.dicoding.tourismapp.core.data.source.local.room.TourismDatabase
import com.dicoding.tourismapp.core.data.source.remote.RemoteDataSource
import com.dicoding.tourismapp.core.data.source.remote.network.ApiService
import com.dicoding.tourismapp.core.domain.repository.ITourismRepository
import com.dicoding.tourismapp.core.utils.AppExecutors
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import okhttp3.CertificatePinner

val databaseModule = module {
    factory { get<TourismDatabase>().tourismDao() } // get<TourismDatabase> retrieve TourismDatabase instance from `single`
    single { // single means it defines a "singleton"
        val passphrase: ByteArray = SQLiteDatabase.getBytes("dicoding".toCharArray()) // create passphrase as ByteArray for database encryption
        val factory = SupportFactory(passphrase) // configure database encryption setting by making SupportFactory
        Room.databaseBuilder(
            androidContext(),
            TourismDatabase::class.java, "Tourism.db"
        )
            .fallbackToDestructiveMigration()
            .openHelperFactory(factory) // enable database encryption with variable `factory` that has been made
            .build()
    } // don't need to write `single<TourismDatabase> {...}` because it's clear that the returned instance of `TourismDatabase` is `Room.databaseBuilder()`
}

val networkModule = module {
    single {
        val hostname = "tourism-api.dicoding.dev"
        val certificatePinner = CertificatePinner.Builder()
            .add(hostname, "sha256/0BFUiL2XBR0AUiH9cwvtU7vw+erNN1BIHCGPBI4QC9U=") // get it from `https://www.ssllabs.com/ssltest`
            .add(hostname, "sha256/jQJTbIh0grw0/1TkHSumWb+Fs0Ggogr621gT3PvPKG0=")
            .add(hostname, "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=")
            .build()
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .certificatePinner(certificatePinner) // add certificatePinner to OkHttpClient
            .build()
    }
    single {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://tourism-api.dicoding.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
        retrofit.create(ApiService::class.java)
    }
}

val repositoryModule = module {
    single { LocalDataSource(get()) }
    single { RemoteDataSource(get()) }
    factory { AppExecutors() }
    single<ITourismRepository> { TourismRepository(get(), get(), get()) } // use `<ITourismRepository>` because `TourismRepository` extends `ITourismRepository`
}