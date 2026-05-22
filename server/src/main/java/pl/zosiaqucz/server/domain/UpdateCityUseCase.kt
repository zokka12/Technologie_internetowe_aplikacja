package pl.zosiaqucz.server.domain

class UpdateCityUseCase(private val repository: CityRepository) {

    suspend fun execute(id: String, attractions: Double, safety: Double, food: Double, status: String): Boolean {
        return repository.updateCityDetails(id, attractions, safety, food, status)
    }
}