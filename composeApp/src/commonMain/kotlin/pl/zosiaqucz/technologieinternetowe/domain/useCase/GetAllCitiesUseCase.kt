package pl.zosiaqucz.technologieinternetowe.domain.useCase

import kotlinx.coroutines.flow.Flow
import pl.zosiaqucz.technologieinternetowe.domain.model.EuropeanCity
import pl.zosiaqucz.technologieinternetowe.domain.repository.CitiesRepository

class GetAllCitiesUseCase(
    private val repository: CitiesRepository
) {
    operator fun invoke(): Flow<List<EuropeanCity>> {
        return repository.getAllCities()
    }
}