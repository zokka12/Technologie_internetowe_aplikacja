package pl.zosiaqucz.technologieinternetowe.domain.useCase

import pl.zosiaqucz.technologieinternetowe.domain.repository.CitiesRepository

class ToggleCityVisitedUseCase(
    private val repository: CitiesRepository
) {
    suspend operator fun invoke(cityId: String) {
        repository.toggleCityVisited(cityId)
    }
}