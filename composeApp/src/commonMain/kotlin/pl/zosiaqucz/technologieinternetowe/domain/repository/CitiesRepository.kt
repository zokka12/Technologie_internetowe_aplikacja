package pl.zosiaqucz.technologieinternetowe.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.zosiaqucz.technologieinternetowe.domain.model.EuropeanCity

interface CitiesRepository {
    fun getAllCities(): Flow<List<EuropeanCity>>
    suspend fun toggleCityVisited(cityId: String)
    suspend fun toggleCityToVisit(cityId: String)
    suspend fun updateCityUserRating(cityId: String, newRating: Double)
}