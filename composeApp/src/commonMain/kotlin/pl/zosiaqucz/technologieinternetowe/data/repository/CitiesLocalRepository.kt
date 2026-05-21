package pl.zosiaqucz.technologieinternetowe.data.repository

import kotlinx.coroutines.flow.Flow
import pl.zosiaqucz.technologieinternetowe.data.dataSource.CitiesLocalDataSource
import pl.zosiaqucz.technologieinternetowe.domain.model.EuropeanCity
import pl.zosiaqucz.technologieinternetowe.domain.repository.CitiesRepository

class CitiesLocalRepository : CitiesRepository {
    override fun getAllCities(): Flow<List<EuropeanCity>> {
        return CitiesLocalDataSource.cities
    }

    override suspend fun toggleCityVisited(cityId: String) {
        CitiesLocalDataSource.toggleCityVisited(cityId)
    }
    override suspend fun toggleCityToVisit(cityId: String) {
        CitiesLocalDataSource.toggleCityToVisit(cityId)
    }
    override suspend fun updateCityUserRating(cityId: String, newRating: Double) {
        CitiesLocalDataSource.updateCityUserRating(cityId, newRating)
    }
}