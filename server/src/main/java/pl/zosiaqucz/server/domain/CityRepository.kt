package pl.zosiaqucz.server.domain // Dopasuj początek do swojej paczki

interface CityRepository {
    suspend fun getAllCities(): List<Any> // Pamiętaj, żeby później zamienić 'Any' na nazwę swojej klasy miasta
    suspend fun updateCityDetails(id: String, attractions: Double, safety: Double, food: Double, status: String): Boolean
}