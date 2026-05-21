package pl.zosiaqucz.technologieinternetowe.domain.useCase

import pl.zosiaqucz.technologieinternetowe.domain.repository.CitiesRepository

class UpdateCityUserRatingUseCase(private val repository: CitiesRepository) {
    suspend operator fun invoke(cityId: String, newRating: Double) {
        repository.updateCityUserRating(cityId, newRating)
    }
}