package pl.zosiaqucz.server.di

import org.koin.dsl.module
import pl.zosiaqucz.server.domain.CityRepository
import pl.zosiaqucz.server.domain.UpdateCityUseCase
import pl.zosiaqucz.server.infrastructure.CityRepositoryImpl

// To jest "przepis" dla Koina
val appModule = module {
    // Kiedy ktoś poprosi o CityRepository, daj mu CityRepositoryImpl
    single<CityRepository> { CityRepositoryImpl() }

    // Kiedy ktoś poprosi o UpdateCityUseCase, zbuduj go, używając gotowego już repozytorium z linijki wyżej (funkcja get())
    single { UpdateCityUseCase(get()) }
}